Git global setup
git config --global user.name "xxxx"
git config --global user.email "xxxx@tencent.com"

Create a new repository
git clone http://git.tmeoa.com/TMEIT/public/idata/dolphinscheduler.git
cd dolphinscheduler
git switch -c main
touch README.md
git add README.md
git commit -m "add README"
git push -u origin main

Push an existing folder
cd existing_folder
git init --initial-branch=main
git remote add origin http://git.tmeoa.com/TMEIT/public/idata/dolphinscheduler.git
git add .
git commit -m "Initial commit"
git push -u origin main

git init # 把项目初始化,相当于在项目的跟目录生成一个 .git 目录
git add . # 把项目的所有文件加入暂存区
git commit -am '项目初始化' # 把项目提交到本地仓库，引号里面的是这次提交的注释，方便以后查看。
git remote rm origin # 先删除远程 Git 仓库
git remote add origin https://github.com/BobinYang/   #为本地的仓库创建一个远程仓库. 例如：git remote add
origin https://github.com/BobinYang/HtmlAgilityPackSample.git
git pull --rebase origin master # 把远端仓库中的代码 拉到本地进得合并一下。
git push --set-upstream origin main

Push an existing Git repository
cd existing_repo
git remote rename origin old-origin
git remote add origin http://git.tmeoa.com/TMEIT/public/idata/dolphinscheduler.git
git push -u origin --all
git push -u origin --tags

自己尝试方法：
1、同时将远程github代码和司内gitlab创建的空项目pull到本地
2、将github下载项目目录下所有文件copy到gitlab本地项目目录下
3、git add .
4、git commit -m "init"
5、git push
通过以上5步即可，但历史更新记录信息会丢失；如果想要历史记录保留需要使用上面的Push an existing Git repository方法；

git将本地已有的项目上传到GitHub
1.首先在项目目录下初始化本地仓库
git init
2.添加所有文件( . 表示所有)
git add .
3.提交所有文件到本地仓库
git commit -m “备注信息”
4.连接到远程仓库
git remote add origin 你的远程仓库地址
5.将项目推送到远程仓库
git push -u origin master

使用命令git checkout -b test,创建test分支，并切换到test分支
使用命令git push origin test,将test分支推到远程仓库

本地 git 项目如何关联多个 remote 源 
git remote add github https://github.com/your-username/your-repo.git 