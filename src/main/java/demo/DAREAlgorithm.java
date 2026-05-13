package demo;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 输入：前置输入 1：同层级所有评估因子，按重要性从高到低完成排序
 * 前置输入 2：相邻两个因子之间的重要性环比倍数
 */
/**
 * 输出：各评估因子的归一化权重值
 * 不是原始打分、不是评价等级、不是灰区间，只输出权重向量。
 */

/**
 * 环比评分法（DARE）算法实现
 * 功能：根据指标重要性排序 + 相邻环比倍数，自动计算权重
 * 适用于：主观赋权、无数据场景、试验鉴定指标权重计算
 */
public class DAREAlgorithm {

    /**
     * 环比评分法计算权重
     * @param ratios 相邻指标的环比重要性倍数（数组长度 = 指标数 - 1）
     *               例如 [1.5, 1.2] 表示 A比B重要1.5倍，B比C重要1.2倍
     * @return 归一化后的权重数组
     */
    public static double[] calculateWeights(double[] ratios) {
        int n = ratios.length + 1; // 指标个数 = 环比数 + 1
        double[] tempCoefficients = new double[n]; // 暂定重要性系数

        // 1. 最后一个指标暂定系数 = 1
        tempCoefficients[n - 1] = 1.0;

        // 2. 从后往前递推计算所有暂定系数
        for (int i = n - 2; i >= 0; i--) {
            tempCoefficients[i] = tempCoefficients[i + 1] * ratios[i];
        }

        // 3. 求和
        double sum = 0.0;
        for (double num : tempCoefficients) {
            sum += num;
        }

        // 4. 归一化得到权重
        double[] weights = new double[n];
        for (int i = 0; i < n; i++) {
            weights[i] = tempCoefficients[i] / sum;
        }

        return weights;
    }

    /**
     * 格式化输出（保留4位小数）
     */
    private static String format(double num) {
        return new BigDecimal(num).setScale(4, RoundingMode.HALF_UP).toString();
    }

    // ====================== 测试示例 ======================
    public static void main(String[] args) {
        // 示例：3个指标 A(工作能力)、B(工作态度)、C(出勤率)
        // 环比值：A比B重要1.5倍，B比C重要1.2倍
        double[] ratios = {1.5, 1.2};

        // 计算权重
        double[] weights = calculateWeights(ratios);

        // 输出结果
        System.out.println("===== 环比评分法（DARE）计算结果 =====");
        System.out.println("指标A（工作能力）权重：" + format(weights[0]));
        System.out.println("指标B（工作态度）权重：" + format(weights[1]));
        System.out.println("指标C（出勤率）权重：" + format(weights[2]));
    }
}
