

## dolphin schedule调度调研

1、dolphinScheduler如何实现去中心化

	Master/Worker注册到Zookeeper中，通过Zookeeper 监听节点变化，切分task实现Master集群和Worker集群无中心

2、dolphinScheduler的作业调度信息存储在Mysql，如何实现分布式调度

	 master在masterSchedulerService 从数据库读取command时findOneCommand()  中按command.getId() % ServerNodeManager.MASTER_SIZE == slot 切分command实现多master 共同执行 

3、分布式调度时，多master操作mysql如何避免信息同步修改问题

	master读取command的时候已做切分，同时操作数据库设置了事务

4、调度模块是否可拆分？如果拆分需要做哪些工作

		拆分Quartz调度模块需要自定义job 实现execute方法 execute()由JobRunShell调用 dolphinScheduler实现了ProcessScheduleJob继承Job类 重写execute 增加了deleteJob

5、作业状态管理如何进行缓存的？类似我们现在遇到的伪分布式中JVM中map缓存如何分布式共享

	主要缓存是processInstanceExecMaps ，作业调度，故障迁移都是 直接更新到数据库

6、QuartzExecutors 使用

	由web api  scheduleController控制 Quartz调度模块需要执行的作业

7、EventExecuteService 作用

	处理workflowExecuteThread的event 遍历processInstanceExecMaps 分发taskevent到不同的host	

#### 一、dolphinScheduler 去中心化

流程图

https://dolphinscheduler.apache.org/zh-cn/docs/latest/user_doc/architecture/configuration.html

DolphinScheduler的去中心化是Master/Worker注册到Zookeeper中，通过Zookeeper 监听节点变化，切分task实现Master集群和Worker集群无中心

由serverNodeManager负责同步

1.定时每10s从数据库t_ds_worker_group和Zookeeper节点同步  workerNodeInfo workerGroupNodes信息

2.启动master节点监听器 订阅 Zookeeper节点 /nodes/master  add remove事件 更新masterNodes masterPriorityQueue	MASTER_SIZE	SLOT_LIST

	masterNodes 是剔除了heartBeat为null 且是按从新到旧创建的Zookeeper master节点arrayList

	masterPriorityQueue和masterNodes 一致

	MASTER_SIZE  是Zookeeper /nodes/master节点数

	SLOT_LIST 是存储该节点在masterPriorityQueue的索引

	get_slot都是取的SLOT_LIST.get(0) 也就是最新的节点Index

3.启动worker节点监听器 订阅 Zookeeper节点 /nodes/worker  add remove update事件 更新workerGroupNodes workerNodeInfo

4.master在masterSchedulerService 从数据库读取command时findOneCommand()  中按command.getId() % ServerNodeManager.MASTER_SIZE == slot 切分command实现多master 共同执行 去中心化

```
spring初始化bean的时候执行
#org.apache.dolphinscheduler.server.master.registry.ServerNodeManager#afterPropertiesSet
	load()
		updateMasterNodes()
		#锁 /lock/masters
		registryClient.getLock(nodeLock)
		syncMasterNodes(currentNodes, masterNodes)
			#锁 /lock/masters  更新masterNodes masterPriorityQueue	MASTER_SIZE	SLOT_LIST
			#masterNodes 剔除了heartBeat为null 且是按从新到旧创建的Zookeeper master节点arrayList
			masterPriorityQueue.putList(masterNodes)
			MASTER_SIZE = nodes.size()
			#SLOT_LIST存储该节点在masterPriorityQueue的索引
            SLOT_LIST.add(masterPriorityQueue.getIndex(NetUtils.getHost()))
    #定时每10s同步workerNodeInfo workerGroupNodes信息
	WorkerNodeInfoAndGroupDbSyncTask
		org.apache.dolphinscheduler.server.master.registry.ServerNodeManager.WorkerNodeInfoAndGroupDbSyncTask#run
			syncAllWorkerNodeInfo(newWorkerNodeInfo)
			定时从数据库同步  workerGroupNodes信息
			List<WorkerGroup> workerGroupList = workerGroupMapper.queryAllWorkerGroup()
			syncWorkerGroupNodes(workerGroup, nodes)
	#对Zookeeper /nodes/master节点注册监听器 订阅事件
	registryClient.subscribe(REGISTRY_DOLPHINSCHEDULER_MASTERS, new MasterDataListener())
		#org.apache.dolphinscheduler.server.master.registry.ServerNodeManager.MasterDataListener#notify
		#锁/lock/masters add remove事件  更新masterPriorityQueue	MASTER_SIZE	SLOT_LIST
		updateMasterNodes()
	#对Zookeeper /nodes/worker节点注册监听器 订阅事件
    registryClient.subscribe(REGISTRY_DOLPHINSCHEDULER_WORKERS, new WorkerDataListener())
		org.apache.dolphinscheduler.server.master.registry.ServerNodeManager.WorkerDataListener#notify
		#锁workerGroupLock add remove update事件 更新workerGroupNodes workerNodeInfo
		syncWorkerGroupNodes(group, currentNodes)
		syncSingleWorkerNodeInfo(node, data)
```



以master为例

```
 初始化netty服务器，并启动
 容错处理
 	通过zookeeper客户端Curator创建一个znode临时节点 /dolphinscheduler/nodes/master/<ip>:<port>，如果主机因为宕机，网络等问题，临时节点会消失
 	通过zookeeper客户端Curator对上面的znode注册监听器 （监听断开连接，重新连接，中止事件）因为是临时节点所以重新连接需要重新创建节点
 	尝试获取 znode节点 /dolphinscheduler/lock/failover/startup-masters 的分布式锁 调用了mutex.acquire();获取锁，
 启动一个Master的zk客户端
 启动eventExecuteService服务
 启动master scheduler 服务
 启动failoverExecute 服务
 启动quartz 定时任务服务

 添加一个jvm的钩子 当jvm关闭时，可以优雅的停止掉服务
```

Master-Server节点  容错处理

```
MasterServer.masterRegistryClient.init
	new NamedThreadFactory("HeartBeatExecutor")
#节点可停止
MasterServer.masterRegistryClient.setRegistryStoppable(this)
	registryClient.setStoppable(stoppable)
MasterServer.masterRegistryClient.start
	#nodeLock: /lock/failover/startup-masters
	registryClient.getLock(nodeLock)
		#分布式公平可重入互斥锁
		#org.apache.dolphinscheduler.plugin.registry.zookeeper.ZookeeperRegistry#acquireLock
			interProcessMutex.acquire()
			#zookeeper多个节点同时在一个指定的节点下面创建临时会话顺序节点，谁创建的节点序号最小，谁就获得了锁，并且其他节点就会监听序号比自己小的节点，一旦序号比自己小的节点被删除了，其他节点就会得到相应的事件，然后查看自己是否为序号最小的节点，如果是，则获取锁			
			#org.apache.curator.framework.recipes.locks.LockInternals#internalLockLoop
			#https://www.jianshu.com/p/fb27dd1036e2
	registryClient.registry()
		new HeartBeatTask(startupTime,
                masterConfig.getMasterMaxCpuloadAvg(),
                masterConfig.getMasterReservedMemory(),
                Sets.newHashSet(getMasterPath()),
                Constants.MASTER_TYPE,
                registryClient)
		#先remove临时节点localNodePath：/nodes/master/ip:端口 
		registryClient.remove(localNodePath)
		#再注册临时节点localNodePath
		registryClient.persistEphemeral(localNodePath, heartBeatTask.getHeartBeatInfo())
		#等待节点注册成功
		while (!registryClient.checkNodeExists(NetUtils.getHost(), NodeType.MASTER)) {
            ThreadUtils.sleep(SLEEP_TIME_MILLIS);
        }
        #等待1s 等待故障迁移线程
        ThreadUtils.sleep(SLEEP_TIME_MILLIS);
        #删除故障master节点
        registryClient.handleDeadServer(Collections.singleton(localNodePath), NodeType.MASTER, Constants.DELETE_OP);
        	#org.apache.dolphinscheduler.service.registry.RegistryClient#removeDeadServerByHost
        	#删除 zookeeper子节点: /dead-servers
        	#org.apache.dolphinscheduler.plugin.registry.zookeeper.ZookeeperRegistry#delete
        	client.delete()
                  .deletingChildrenIfNeeded()
                  .forPath(nodePath);	
        #添加Zookeeper 节点监听器
        registryClient.addConnectionStateListener(this::handleConnectionState);
        	#org.apache.dolphinscheduler.plugin.registry.zookeeper.ZookeeperRegistry#addConnectionStateListener
        	client.getConnectionStateListenable().addListener(new ZookeeperConnectionStateListener(listener));
        #定时线程启动心跳	
        this.heartBeatExecutor.scheduleAtFixedRate(heartBeatTask, masterHeartbeatInterval, masterHeartbeatInterval, TimeUnit.SECONDS);
        	#org.apache.dolphinscheduler.server.registry.HeartBeatTask#run
        		#检测此节点是否健康 不健康则关闭Master
        		if (registryClient.checkIsDeadServer(heartBeatPath, serverType)) {
                    registryClient.getStoppable().stop("i was judged to death, release resources and stop myself");
                    return;
                }
        		#获取工作线程池可用线程数
        		heartBeat.setWorkerWaitingTaskCount(workerManagerThread.getThreadPoolQueueSize());
        		#注册临时节点localNodePath
        		registryClient.persistEphemeral(heartBeatPath, heartBeat.encodeHeartBeat());
    #zookeeper 监听/nodes    		
	registryClient.subscribe(REGISTRY_DOLPHINSCHEDULER_NODE, new MasterRegistryDataListener())
	catch (Exception e)
		this.registryClient.getStoppable().stop("master start up exception");
	finally
		#释放锁
		registryClient.releaseLock(nodeLock)
```

FailoverExecuteThread

```
#获取需要故障迁移的master节点
hosts = getNeedFailoverMasterServers()
	hosts = processService.queryNeedFailoverProcessInstanceHost()
		#查找processInstance表
		processInstanceMapper.queryNeedFailoverProcessInstanceHost(stateArray)
failoverPath = masterRegistryClient.getFailoverLockPath(NodeType.MASTER, host)
#/lock/failover/masters 获取节点lock
registryClient.getLock(failoverPath)
#failover process instance and associated task instance
masterRegistryClient.failoverMaster(host)
	#获取节点启动时间
	serverStartupTime = getServerStartupTime(NodeType.MASTER, masterHost)
	#获取worker节点
	workerServers = registryClient.getServerList(NodeType.WORKER)
	#需要迁移的ProcessInstanceList
	needFailoverProcessInstanceList = processService.queryNeedFailoverProcessInstances(masterHost)
		processInstanceMapper.queryByHostAndStatus(host, stateArray)
	#校验后的TaskInstanceList 
	#移除taskInstance.getHost()为null taskInstance.getState().typeIsFinished()
	#过滤掉taskInstance.getHost()为null taskInstance.getSubmitTime()为null 和 workerServerStartDate启动时间 大于 taskTime task启动时间的taskInstance
	checkTaskInstanceNeedFailover(workerServers, taskInstance)
	#迁移task   
    failoverTaskInstance(processInstance, taskInstance)
    	#kill yarn job
    	ProcessUtils.killYarnJob(taskExecutionContext)
    	#taskInstance设置作业状态为8 need fault tolerance
    	taskInstance.setState(ExecutionStatus.NEED_FAULT_TOLERANCE)
    	#更新到数据库taskInstance
        processService.saveTaskInstance(taskInstance)
        	#更新到数据库update Or insert
        	taskInstanceMapper.updateById(taskInstance)
        	taskInstanceMapper.insert(taskInstance)
        #获取WorkflowExecuteThread ： master exec thread,split dag
      	workflowExecuteThreadNotify = processInstanceExecMaps.get(processInstance.getId())
      	#设置一个类型为task state change的事件 
        workflowExecuteThreadNotify.addStateEvent(stateEvent)
    #有事务注解@Transactional    生成一条类型为recover tolerance fault process的命令插入command表
    processService.processNeedFailoverProcessInstances(processInstance)  
    	#设置processInstance Host为null
    	processInstance.setHost(Constants.NULL)
    	#更新到processInstance表中
    	processInstanceMapper.updateById(processInstance)
  		#processDefinition -> Command
  		#设置command类型为recover tolerance fault process
    	cmd.setCommandType(CommandType.RECOVER_TOLERANCE_FAULT_PROCESS)
    	#插入command表
    	createCommand(cmd)
```



#### 二、dolphinScheduler   masterSchedulerService

1.在scheduleProcess() 中获取command 按command.getId() % ServerNodeManager.MASTER_SIZE == slot 切分command 给不同的master实现分布式调度

2.处理command 时handleCommand方法 将command转化为ProcessInstance会更新ProcessInstance 表  更新ProcessInstanceMap表

更新后从Command表删除这条命令

handleCommand添加了@Transactional注解,对数据库操作配置了事务

submitTask 添加了@Transactional注解

```
#org.apache.dolphinscheduler.server.master.runner.MasterSchedulerService#start
super.start();
	org.apache.dolphinscheduler.server.master.runner.MasterSchedulerService#run
		#检测线程是否有资源
		OSUtils.checkResource(masterConfig.getMasterMaxCpuloadAvg(), masterConfig.getMasterReservedMemory())
		#有则执行，无则sleep 1s
		scheduleProcess()
			#获取命令  查找数据库 按command.getId() % ServerNodeManager.MASTER_SIZE == slot 切分command
			Command command = findOneCommand()
			#org.apache.dolphinscheduler.server.master.runner.MasterSchedulerService#findOneCommand
				List<Command> commandList = processService.findCommandPage(ServerNodeManager.MASTER_SIZE, pageNumber)
				#org.apache.dolphinscheduler.service.process.ProcessService#findCommandPage
					commandMapper.queryCommandPage(pageSize, pageNumber * pageSize)
					#org.apache.dolphinscheduler.dao.mapper.CommandMapper#queryCommandPage
			#获取命令结果不为空，处理命令
			#org.apache.dolphinscheduler.service.process.ProcessService#handleCommand
			#handleCommand添加了@Transactional注解,对数据库操作配置了事务 
			ProcessInstance processInstance = processService.handleCommand(logger, getLocalAddress(), command)
			 	#更新ProcessInstance 表
				saveProcessInstance(processInstance)
				#处理SubProcess
				this.setSubProcessParam(processInstance)
					this.saveProcessInstance(subProcessInstance)
					updateWorkProcessInstanceMap(processInstanceMap)
				#从Command表删除这条命令
				this.deleteCommandWithCheck(command.getId())
			#使用FutureCallback执行
			masterExecService.execute(workflowExecuteThread)
				#WorkflowExecuteThread：master exec thread,split dag
				#org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteThread#startProcess
					#构建dag
					buildFlowDag()
						dag = DagHelper.buildDagGraph(processDag)
            		initTaskQueue()
            			processService.updateProcessInstance(processInstance)
            		submitPostNode(null)
            			#dag读取都需要lock 
            			org.apache.dolphinscheduler.dao.utils.DagHelper#parsePostNodes
            					lock.readLock().lock();
            			submitStandByTask()
            				#遍历 readyToSubmitTaskQueue submitTaskNodeList-> taskInstances
            				#org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteThread#submitTaskExec
            				#提交作业
            				TaskInstance taskInstance = submitTaskExec(task)
            					#根据task type不同 创建不同的taskProcessor：BaseTaskProcessor CommonTaskProcessor ConditionTaskProcessor DependentTaskProcessor SubTaskProcessor SwitchTaskProcessor
            					ITaskProcessor taskProcessor = TaskProcessorFactory.getTaskProcessor(taskInstance.getTaskType())
            					#if running 则远程发送命令给worker执行作业
            					notifyProcessHostUpdate(taskInstance)
            						#org.apache.dolphinscheduler.server.master.dispatch.executor.NettyExecutorManager#doExecute
            						nettyExecutorManager.doExecute(host, hostUpdateCommand.convert2Command())
            					#org.apache.dolphinscheduler.server.master.runner.task.BaseTaskProcessor#action
                                #提交submit
                                submit = taskProcessor.action(TaskAction.SUBMIT)
                                submitTask 添加了@Transactional注解
                                	#org.apache.dolphinscheduler.service.process.ProcessService#submitTaskInstanceToDB
                                	TaskInstance task = submitTaskInstanceToDB(taskInstance, processInstance)
                                		#org.apache.dolphinscheduler.service.process.ProcessService#updateTaskInstance
                                		
                                #启动run
                                taskProcessor.action(TaskAction.RUN)
                              
						#org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteThread#updateProcessInstanceState()
						updateProcessInstanceState()
							#从数据库获取ProcessInstance
							ProcessInstance instance = processService.findProcessInstanceById(processInstance.getId())
								processInstanceMapper.selectById(processId)
							processService.updateProcessInstance(instance)
								processInstanceMapper.updateById(processInstance)
							#org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteThread#processStateChangeHandler
							this.processStateChangeHandler(stateEvent)
								#org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteThread#processComplementData
								#if STOP
									this.updateProcessInstanceState(stateEvent)
								if (processComplementData())
									createComplementDataCommand(scheduleDate)
										processService.createCommand(command)
           										processService.createCommand(command)
           										#org.apache.dolphinscheduler.service.process.ProcessService#createCommand
           										result = commandMapper.insert(command)
								#if finished
									endProcess()
								#if ready_stop
                                	killAllTasks()

#重试task
	timeout check
	dependent task 
this.stateWheelExecuteThread.start()
	check4StartProcessFailed();
    checkTask4Timeout();
    checkTask4Retry();
    checkProcess4Timeout();
```


	

#### 三、dolphinScheduler   QuartzExecutors

定时调度是由master直接启动 将schedule转化为command 插入数据库command

```
QuartzExecutors.getInstance().start()
	在构造函数中init
	#org.apache.dolphinscheduler.service.quartz.QuartzExecutors#init
	scheduler = schedulerFactory.getScheduler()
	org.quartz.core.QuartzScheduler#start
	#执行
	org.apache.dolphinscheduler.service.quartz.ProcessScheduleJob#execute
		#由context获取dataMap
		dataMap = context.getJobDetail().getJobDataMap()
		#查找数据库获取schedule
		schedule = getProcessService().querySchedule(scheduleId)
			scheduleMapper.selectById(id)
		#schedule是null或schedule状态是offline则deleteJob(projectId, scheduleId)
			scheduler.deleteJob(jobKey)
		#schedule转化为command
		getProcessService().createCommand(command)
		#插入数据库
		commandMapper.insert(command)
```

QuartzExecutors.addJob()方法由api server 在controller层被调用 

```
org.apache.dolphinscheduler.api.service.impl.SchedulerServiceImpl#setScheduleState

#org.apache.dolphinscheduler.api.service.impl.SchedulerServiceImpl#setSchedule
setSchedule(project.getId(), scheduleObj);
	QuartzExecutors.getInstance().addJob(ProcessScheduleJob.class, projectId, schedule)
		org.apache.dolphinscheduler.service.quartz.QuartzExecutors#addJob
			scheduleObj -> jobDataMap -> jobDetail (ProcessScheduleJob) -> schedule.addJob -> CronTrigger.forJob(jobDetail).build() -> scheduler.scheduleJob(cronTrigger)
```

拆分Quartz调度模块需要自定义job 实现execute方法 execute()由JobRunShell调用

dolphinScheduler实现了ProcessScheduleJob继承Job类 重写execute 增加了deleteJob



由web api  scheduleController控制 Quartz调度模块需要执行的作业

```
insertSchedule 
updateSchedule
setScheduleState设置定时调度schedule上下线 -> setSchedule or deleteSchedule 
querySchedule 
queryScheduleList 
deleteSchedule 
checkValid 
deleteScheduleById 
previewSchedule 
updateScheduleByProcessDefinitionCode
```

#### 四、dolphinScheduler EventExecuteService

处理workflowExecuteThread的event 遍历processInstanceExecMaps

```
#org.apache.dolphinscheduler.server.master.runner.EventExecuteService#run
#processInstanceExecMaps -> eventHandlerMap
eventHandler()
	#遍历processInstanceExecMaps中的workflowExecuteThread
		eventHandlerMap.put(workflowExecuteThread.getKey(), workflowExecuteThread)
		future = this.listeningExecutorService.submit(workflowExecuteThread)
			onSuccess()
				#当workflow结束
				notifyProcessChanged()
					#获取父processTaskMap
					fatherMaps = processService.notifyProcessList(processInstanceId)
						processInstanceMap = processInstanceMapMapper.queryBySubProcessId(processId)
						#当processInstanceMap中host与自己一致
						notifyMyself(processInstance, fatherMaps.get(processInstance))
							# 添加了一个type为1 task state change 的stateEvent 设置了ExecutionStatus为running
							workflowExecuteThreadNotify.addStateEvent(stateEvent)
						#当processInstanceMap中host与自己不一致
						notifyProcess(processInstance, fatherMaps.get(processInstance))
							#向host发送了类型为state event request的command
							stateEventCallbackService.sendResult(address, port, stateEventChangeCommand.convert2Command())
								nettyRemoteChannel.writeAndFlush(command)
```



#### 五、dolphinScheduler WorkerServer WorkerManagerThread

task execute manager

```
org.apache.dolphinscheduler.server.worker.runner.WorkerManagerThread#run
workerExecService.submit(taskExecuteThread)
	#org.apache.dolphinscheduler.server.worker.runner.TaskExecuteThread#run
	#通过netty更新task最终状态
	finally
		taskCallbackService.sendResult(taskExecutionContext.getTaskInstanceId(), responseCommand.convert2Command());
```

在masterserver中有一个TaskResponseProcessor接收task 执行结果

```
TaskResponseProcessor taskResponseProcessor = new TaskResponseProcessor()
this.nettyRemotingServer.registerProcessor(CommandType.TASK_EXECUTE_RESPONSE, taskResponseProcessor);
org.apache.dolphinscheduler.server.master.processor.TaskResponseProcessor#process
	responseCommand = JSONUtils.parseObject(command.getBody(), TaskExecuteResponseCommand.class);
	taskResponseService.addResponse(taskResponseEvent)
		#添加到event队列中
		eventQueue.put(taskResponseEvent)
		
```

```
#org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteThread#handleEvents
	stateEventHandler(stateEvent)

```



#### TaskPriorityQueueConsumer

```
org.apache.dolphinscheduler.server.master.consumer.TaskPriorityQueueConsumer#run
#获取fetchTaskNum
```

