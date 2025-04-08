package com.demo;

import org.junit.jupiter.api.Test;

public class LCSTest {

    @Test
    public void LCSDemoTest() {
        String str1 = "AGGTAB";
        String str2 = "GXTXAYB";
        System.out.println(longestCommonSubsequence(str1, str2));

        System.out.println("--------------------");

        int m = str1.length();
        int n = str2.length();
        int[][] dp = new int[m + 1][n + 1];
        String lcs = getLongestCommonSubsequence(str1, str2, dp);
        System.out.println(lcs);

    }


    public int longestCommonSubsequence(String str1, String str2) {
        int m = str1.length();
        int n = str2.length();
        // 创建二维数组 dp用于存储子问题的解
        // dp[i][j] 表示 str1 的前 i 个字符和 str2 的前 j 个字符的最长公共子序列的长度
        int[][] dp = new int[m+1][n+1];
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (str1.charAt(i-1) == str2.charAt(j-1)) {
                    // 如果当前字符相等,则最长公共子序列长度在之前的基础上加 1
                    dp[i][j] = dp[i-1][j-1] + 1;
                } else {
                    // 若不相等，则取两种情况的最大值
                    // 情况 1：str1 的前 i - 1 个字符和 str2 的前 j 个字符的最长公共子序列长度
                    // 情况 2：str1 的前 i 个字符和 str2 的前 j - 1 个字符的最长公共子序列长度
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j-1]);
                }
            }
        }
        // 返回最终结果，即 str1 和 str2 的最长公共子序列的长度
        return dp[m][n];
    }


    public String getLongestCommonSubsequence(String str1, String str2, int[][] dp) {
        int m = str1.length();
        int n = str2.length();
        StringBuilder lcs = new StringBuilder();
        int i=m, j=n;
        // 从 dp 数组的右下角开始回溯，直到到达左上角
        while (i>0 && j>0) {
            if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                // 如果当前 str1 和 str2 对应位置的字符相等,将该字符插入到 StringBuilder 的开头，因为是从后往前回溯
                lcs.insert(0, str1.charAt(i-1));
                // 向左上方移动，即同时减少 i 和 j
                i--;
                j--;
            } else if (dp[i - 1][j] > dp[i][j - 1]) {
                // 如果不相等，比较上方和左方的值,如果上方的值更大，向上移动，减少 i
                i--;
            } else {
                // 否则，向左移动，减少 j
                j--;
            }
        }

        return lcs.toString();
    }
}
