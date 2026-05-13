package demo;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 输入：必须需要评估因子
 * 必须先划分层级：目标层、准则层、指标层，每层有若干同级评估因子；
 */
/**
 * 输出
 * 场景 1：只做赋权（常用）
 * 输入：同级因子 + 判断矩阵
 * 输出：该层各因子归一化权重值 + 最大特征值 λmax、CI、CR 一致性检验结果
 *  只出权重，不出评估分值。
 * 场景 2：完整综合评估（逐层加权）
 * 输入：因子 + 判断矩阵 + 底层指标单项得分
 * 输出：
 * 每层因子权重
 * 自底向上加权计算得到上层综合评估值 / 最终总分 / 评价等
 */

/**
 * 层次分析法（AHP）评估工具类
 * 支持：判断矩阵权重计算 + 一致性检验 + 综合评分
 */
public class AHPUtil {

    // 平均随机一致性指标 RI（1~15阶矩阵）
    private static final double[] RI = {
            0.00, 0.00, 0.58, 0.90, 1.12, 1.24, 1.32, 1.41, 1.45,
            1.49, 1.52, 1.54, 1.56, 1.58, 1.59
    };

    /**
     * 计算判断矩阵的权重向量 + 一致性检验
     * @param matrix 判断矩阵（n阶方阵，1-9标度）
     * @return 权重数组
     */
    public static double[] calculateWeights(double[][] matrix) {
        int n = matrix.length;

        // 1. 列归一化
        double[][] normalizedMatrix = normalizeColumns(matrix);

        // 2. 行求和并平均 → 权重
        double[] weights = new double[n];
        for (int i = 0; i < n; i++) {
            double sum = 0;
            for (int j = 0; j < n; j++) {
                sum += normalizedMatrix[i][j];
            }
            weights[i] = sum / n;
        }

        // 3. 一致性检验
        consistencyCheck(matrix, weights);

        return weights;
    }

    /**
     * 列归一化
     */
    private static double[][] normalizeColumns(double[][] matrix) {
        int n = matrix.length;
        double[][] normMatrix = new double[n][n];

        // 每列求和
        for (int j = 0; j < n; j++) {
            double colSum = 0;
            for (int i = 0; i < n; i++) {
                colSum += matrix[i][j];
            }

            // 归一化
            for (int i = 0; i < n; i++) {
                normMatrix[i][j] = matrix[i][j] / colSum;
            }
        }
        return normMatrix;
    }

    /**
     * 一致性检验
     */
    private static void consistencyCheck(double[][] matrix, double[] weights) {
        int n = matrix.length;

        // 计算最大特征根 λmax
        double lambdaMax = 0;
        for (int i = 0; i < n; i++) {
            double sum = 0;
            for (int j = 0; j < n; j++) {
                sum += matrix[i][j] * weights[j];
            }
            lambdaMax += sum / weights[i];
        }
        lambdaMax /= n;

        // 一致性指标 CI
        double CI = (lambdaMax - n) / (n - 1);

        // 一致性比例 CR
        double CR = CI / RI[n];

        // 输出检验结果
        System.out.println("=====================================");
        System.out.println("一致性检验结果：");
        System.out.println("最大特征根 λmax = " + format(lambdaMax));
        System.out.println("一致性指标 CI = " + format(CI));
        System.out.println("随机一致性指标 RI = " + RI[n]);
        System.out.println("一致性比例 CR = " + format(CR));
        if (CR < 0.1) {
            System.out.println("✅ 检验通过：CR < 0.1，判断矩阵有效");
        } else {
            System.out.println("❌ 检验失败：CR ≥ 0.1，判断矩阵需修正！");
        }
        System.out.println("=====================================\n");
    }

    /**
     * 计算方案综合得分
     * @param criteriaWeights 准则层权重
     * @param scores 方案在各准则下的得分（方案数 × 准则数）
     * @return 各方案最终得分
     */
    public static double[] calculateScore(double[] criteriaWeights, double[][] scores) {
        int schemeNum = scores.length;
        double[] result = new double[schemeNum];

        for (int i = 0; i < schemeNum; i++) {
            double sum = 0;
            for (int j = 0; j < criteriaWeights.length; j++) {
                sum += criteriaWeights[j] * scores[i][j];
            }
            result[i] = sum;
        }
        return result;
    }

    /**
     * 格式化输出（保留4位小数）
     */
    private static String format(double num) {
        return new BigDecimal(num).setScale(4, RoundingMode.HALF_UP).toString();
    }

    // ====================== 测试示例 ======================
    public static void main(String[] args) {
        // 示例：对象评估（3个准则：性能、价格、易用性）
        // 1. 准则层判断矩阵（3阶）
        double[][] criteriaMatrix = {
                {1, 3, 5},
                {1.0/3, 1, 2},
                {1.0/5, 1.0/2, 1}
        };

        // 计算准则权重
        double[] criteriaWeights = calculateWeights(criteriaMatrix);
        System.out.println("📊 准则层权重：");
        System.out.println("性能权重：" + format(criteriaWeights[0]));
        System.out.println("价格权重：" + format(criteriaWeights[1]));
        System.out.println("易用性权重：" + format(criteriaWeights[2]));
        System.out.println();

        // 2. 方案得分（2个评估对象，在3个准则下的得分）
        double[][] schemeScores = {
                {85, 70, 90},   // 对象1得分
                {75, 90, 80}    // 对象2得分
        };

        // 3. 计算综合评分
        double[] finalScores = calculateScore(criteriaWeights, schemeScores);
        System.out.println("🏆 评估对象综合得分：");
        for (int i = 0; i < finalScores.length; i++) {
            System.out.println("对象" + (i + 1) + "：" + format(finalScores[i]) + " 分");
        }
    }
}
