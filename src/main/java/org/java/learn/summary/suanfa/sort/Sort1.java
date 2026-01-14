package org.java.learn.summary.suanfa.sort;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author : duyanlong
 * @version V1.0
 * @Project: java_summary
 * @Package org.java.learn.summary.suanfa.sort
 * @Description: TODO
 * @date Date : 2019年08月27日 20:41
 */
public class Sort1 {

    public static void main(String[] args) throws InterruptedException {

        Semaphore semaphore = new Semaphore(0);
        int[] arrays = {24,35,2,12,5,89,45,100,4,33};

        System.out.println("冒泡排序");
        System.out.println("================");
        printArr(maopao(arrays.clone()));
        System.out.println();
        System.out.println();
        System.out.println("快速排序");
        System.out.println("================");
        printArr(kuaisu(arrays.clone()));
        System.out.println();
        System.out.println();
        System.out.println("选择排序");
        System.out.println("================");
        printArr(xuanzhe(arrays.clone()));
        System.out.println();
        System.out.println();
        System.out.println("插入排序");
        System.out.println("================");
        printArr(charu(arrays.clone()));


    }

    static void printArr(int[] arrays){
        for (int a:arrays){
            System.out.println(a);
        }
    }

    /**
     * 冒泡
     * @param arr
     * @return
     */
    static int[] maopao(int[] arr){

        for(int i=0;i<arr.length;i++){
            for(int j = i+1;j<arr.length;j++){
                if(arr[i]>arr[j]){
                    int tmp = arr[i];
                    arr[i] = arr[j];
                    arr[j] = tmp;
                }
            }
        }

        return arr;
    }

    /**
     * 快速
     * @param arr
     * @return
     */
    static int[] kuaisu(int[] arr){
        kuaisu1(arr,0,arr.length-1);
        return arr;
    }

    static void kuaisu1(int[] arr,int start,int end){
        if(start<end){
            int left = start;
            int right = end;
            int baseNum = arr[start];
            int tmp;
            while(left<right){
                while (baseNum>arr[left]&&left<end){
                    left++;
                }
                while(baseNum<arr[right]&&right>start){
                    right--;
                }
                if(left<=right){
                    tmp = arr[left];
                    arr[left] = arr[right];
                    arr[right] = tmp;
                    left++;
                    right--;
                }

            }
            if(start<right){
                kuaisu1(arr,start,right);
            }
            if(left<end){
                kuaisu1(arr,left,end);
            }
        }

    }

    /**
     * 选择
     * @param arr
     * @return
     */
    static int[] xuanzhe(int[] arr){

        for(int i=0;i<arr.length;i++){

            int min = arr[i];
            int minIdx = i;
            for(int j=i+1;j<arr.length;j++){
                if(min>arr[j]){
                    min = arr[j];
                    minIdx = j;
                }
            }
            if(minIdx!=i){
                arr[minIdx] = arr[i];
                arr[i] = min;
            }
        }

        return arr;
    }

    /**
     * 插入
     * @param arr
     * @return
     */
    static int[] charu(int[] arr){

        int current;
        for (int i=1;i<arr.length;i++){
            current = arr[i];
            int j = i-1;
            while(j>=0&&arr[j]>current){
                arr[j+1]=arr[j];
                j--;
            }
            arr[j+1]=current;
        }

        return arr;
    }

}
