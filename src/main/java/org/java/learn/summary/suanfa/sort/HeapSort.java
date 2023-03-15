package org.java.learn.summary.suanfa.sort;

import java.util.Arrays;

/**
 * @author : duyanlong
 * @version V1.0
 * @Project: java_summary
 * @Package org.java.learn.summary.suanfa.sort
 * @Description: 堆排序
 * 创建一个堆 H[0……n-1]；
    把堆首（最大值）和堆尾互换；
    把堆的尺寸缩小 1，并调用 shift_down(0)，目的是把新的数组顶端数据调整到相应位置；
    重复步骤 2，直到堆的尺寸为 1。
 * @date Date : 2019年09月10日 19:13
 */
public class HeapSort {

    public static void main(String[] args) throws Exception {
        HeapSort heapSort = new HeapSort();
        int[] sortedArr = heapSort.sort(new int[]{3, 4, 251, 55, 33, 21, 87, 9, 6, 185});
        for (int i:sortedArr){
            System.out.println(i);
        }
    }

    public int[] sort(int[] sourceArray) throws Exception {
        // 对 arr 进行拷贝，不改变参数内容
        int[] arr = Arrays.copyOf(sourceArray, sourceArray.length);

        int len = arr.length;

        buildMaxHeap(arr, len);

        for (int i = len - 1; i > 0; i--) {
            swap(arr, 0, i);
            len--;
            heapify(arr, 0, len);
        }
        return arr;
    }

    private void buildMaxHeap(int[] arr, int len) {
        for (int i = (int) Math.floor(len / 2); i >= 0; i--) {
            heapify(arr, i, len);
        }
    }

    private void heapify(int[] arr, int i, int len) {
        int left = 2 * i + 1;
        int right = 2 * i + 2;
        int largest = i;

        if (left < len && arr[left] > arr[largest]) {
            largest = left;
        }

        if (right < len && arr[right] > arr[largest]) {
            largest = right;
        }

        if (largest != i) {
            swap(arr, i, largest);
            heapify(arr, largest, len);
        }
    }

    private void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

}
