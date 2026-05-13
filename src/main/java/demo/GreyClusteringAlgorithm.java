package demo;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 灰数 + 白化权函数 评估方法（灰色聚类评估）
 * 适用于：试验鉴定、贫信息、不确定数据、模糊指标评估
 * 专利可用：灰色白化权函数聚类算法
 */
public class GreyClusteringAlgorithm {

    // 白化权函数类型：三角白化权函数（最常用、最标准）
    public static class GreyFunction {
        double a, b, c;  // 三角白化权函数三个关键点

        public GreyFunction(double a, double b, double c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }

        // 计算白化权值（隶属度）
        public double calculate(double x) {
            if (x <= a || x >= c) return 0.0;
            if (x >= a && x <= b) return (x - a) / (b - a);
            if (x >= b && x <= c) return (c - x) / (c - b);
            return 0.0;
        }
    }

    /**
     * 灰色聚类评估主方法
     * @param indexValue 指标实测值（输入）
     * @param greyFunctions 各灰类的白化权函数（输入）
     * @return 各灰类聚类系数（输出）
     */
    public static double[] greyClustering(double indexValue, GreyFunction[] greyFunctions) {
        int classNum = greyFunctions.length;
        double[] coefficients = new double[classNum];

        // 计算每个灰类的白化权值
        for (int i = 0; i < classNum; i++) {
            coefficients[i] = greyFunctions[i].calculate(indexValue);
        }

        // 归一化聚类系数（标准流程）
        double sum = 0.0;
        for (double v : coefficients) sum += v;
        if (sum > 0) {
            for (int i = 0; i < classNum; i++) {
                coefficients[i] = coefficients[i] / sum;
            }
        }
        return coefficients;
    }

    /**
     * 根据聚类系数判定最终等级
     */
    public static int getEvalLevel(double[] coefficients) {
        int level = 0;
        double max = 0;
        for (int i = 0; i < coefficients.length; i++) {
            if (coefficients[i] > max) {
                max = coefficients[i];
                level = i + 1; // 等级 1 2 3...
            }
        }
        return level;
    }

    // 格式化输出
    private static String fmt(double d) {
        return new BigDecimal(d).setScale(4, RoundingMode.HALF_UP).toString();
    }

    // ==================== 测试 Demo ====================
    public static void main(String[] args) {
        // 场景：装备可靠性指标 → 灰类：差(1)、中(2)、良(3)、优(4)
        // 定义 4 个灰类的三角白化权函数
        GreyFunction[] levels = {
                new GreyFunction(0, 30, 60),     // 1级：差
                new GreyFunction(30, 60, 80),   // 2级：中
                new GreyFunction(60, 80, 95),   // 3级：良
                new GreyFunction(80, 95, 100)   // 4级：优
        };

        // 输入：装备可靠性实测值（试验鉴定实测数据）
        double testValue = 86;

        // 灰色聚类评估
        double[] result = greyClustering(testValue, levels);

        // 输出结果
        System.out.println("===== 灰数白化权函数评估结果 =====");
        System.out.println("指标实测值：" + testValue);
        System.out.println("1级（差）聚类系数：" + fmt(result[0]));
        System.out.println("2级（中）聚类系数：" + fmt(result[1]));
        System.out.println("3级（良）聚类系数：" + fmt(result[2]));
        System.out.println("4级（优）聚类系数：" + fmt(result[3]));

        int evalLevel = getEvalLevel(result);
        System.out.println("\n最终评估等级：" + evalLevel + " 级");
    }
}
