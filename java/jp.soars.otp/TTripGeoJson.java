package jp.soars.otp;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * 複数のトリップデータをGeoJson形式で出力するクラス
 */
public class TTripGeoJson {

    /** 出力ストリーム */
    private PrintWriter fWriter;

    /**
     * コンストラクタ
     * ファイルをオープンする。
     * @param path 出力ファイルのパス
     * @throws FileNotFoundException
     */
    public TTripGeoJson(String path) throws FileNotFoundException {
        fWriter = new PrintWriter(path);
        fWriter.println("{\"type\": \"FeatureCollection\",\"features\": [");
    }

    /**
     * トリップの出力を開始する
     * @return 自分自信
     */
    public TTripGeoJson beginTrip() {
        fWriter.print("{\"type\": \"Feature\",\"properties\": {\"vendor\":  \"A\"},\"geometry\": {\"type\": \"LineString\",\"coordinates\": [");
        return this;
    }

    /**
     * 座標とタイムスタンプを出力する。
     * @param longitude 経度
     * @param latitude 緯度
     * @param altitude 標高
     * @param timestamp タイムスタンプ
     * @return 自分自身
     */
    public TTripGeoJson writePoint(double longitude, double latitude, double altitude, long timestamp) {
        fWriter.print("[" + longitude + "," + latitude + "," + altitude + "," + timestamp + "]");
        return this;
    }

    /**
     * カンマを出力する。
     * @return 自分自身
     */
    public TTripGeoJson comma() {
        fWriter.print(",");
        return this;
    }

    /**
     * トリップの出力を終了する。
     * @return 自分自身
     */
    public TTripGeoJson endTrip() {
        fWriter.print("]}}");
        return this;
    }

    /**
     * 改行を出力する。
     * @return 自分自身
     */
    public TTripGeoJson newline() {
        fWriter.println();
        return this;
    }

    /**
     * ファイルをクローズする。
     */
    public void close() {
        fWriter.println("]}");
        fWriter.close();
    }

    /**
     * ２本のトリップを出力するメソッド
     * @param args
     * @throws FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException {
        TTripGeoJson tgj = new TTripGeoJson("testtrip.json"); //ログファイルをオープンする
        tgj.beginTrip(); //トリップデータを開始
         //座標、タイムスタンプ、カンマを出力
        tgj.writePoint(-74.20986, 40.81773, 0, 1564184363).comma();
        tgj.writePoint(-74.20987, 40.81765, 0, 1564184396).comma();
        tgj.writePoint(-74.20998, 40.81746, 0, 1564184409);
        tgj.endTrip().comma().newline(); //トリップデータを終了後、カンマと改行を出力。
        tgj.beginTrip();  //トリップデータを開始
         //座標、タイムスタンプ、カンマを出力
         tgj.writePoint(-74.20986, 40.81783, 0, 1564184363).comma();
        tgj.writePoint(-74.20987, 40.81775, 0, 1564184396).comma();
        tgj.writePoint(-74.20998, 40.81756, 0, 1564184409);
        tgj.endTrip().newline(); //トリップデータを終了後、改行を出力。
        tgj.close(); //ログファイルをクローズ。
    }

}
