package fpt.org.inblue.utils;

public final class VectorUtils {

    private VectorUtils() {}

    /**
     * Tính độ tương đồng giữa 2 vector và quy đổi trực tiếp ra phần trăm [0.0% - 100.0%].
     * Làm tròn đến 2 chữ số thập phân (ví dụ: 85.65).
     *
     * @param vectorA vector thứ nhất
     * @param vectorB vector thứ hai
     * @return độ tương đồng dạng phần trăm từ 0.0 đến 100.0
     */
    public static double cosineSimilarity(float[] vectorA, float[] vectorB) {
        if (vectorA == null || vectorB == null || vectorA.length == 0 || vectorB.length == 0) {
            return 0.0;
        }
        if (vectorA.length != vectorB.length) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += (double) vectorA[i] * vectorB[i];
            normA += (double) vectorA[i] * vectorA[i];
            normB += (double) vectorB[i] * vectorB[i];
        }

        if (normA <= 0.0 || normB <= 0.0) {
            return 0.0;
        }

        double similarity = dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
        double clamped = Math.max(0.0, Math.min(1.0, similarity));

        // Quy đổi ra phần trăm và làm tròn 2 chữ số thập phân (ví dụ: 0.8565 -> 85.65)
        return Math.round(clamped * 10000.0) / 100.0;
    }
}
