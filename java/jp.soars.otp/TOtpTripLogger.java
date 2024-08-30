package jp.soars.otp;

import java.io.FileNotFoundException;
import java.util.List;

import org.locationtech.jts.geom.LineString;
import org.opentripplanner.common.geometry.SphericalDistanceLibrary;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.spt.GraphPath;

/**
 * TOtpResultをKepler.gl用のトリップデータを出力するロガー
 */
public class TOtpTripLogger {

    /** UTC/協定世界時とJST/日本標準時の時差（秒） */
    static final long TIME_DIFFERENCE_UTC_JST_IN_SECONDS = 9L * 3600L;

    /** 1行目のトリップデータか？ trueの場合、先頭に","をつけないで出力する。 */
    private boolean fFirstFlag = true;

    /** 複数のトリップデータをGeoJson形式で出力するオブジェクト */
    TTripGeoJson fTripGeoJson;

    /**
     * コンストラクタ
     * @param path ログファイルのパス
     * @throws FileNotFoundException
     */
    public TOtpTripLogger(String path) throws FileNotFoundException {
        fTripGeoJson = new TTripGeoJson(path);
    }

    /**
     * LineStringをトリップデータとして出力する。
     * @param tgj 複数のトリップデータをGeoJson形式で出力するオブジェクト
     * @param line LineString
     * @param distance LineStringの長さ
     * @param startTime 始点の時刻
     * @param endTime 終点の時刻
     */
    private void writeLineString(TTripGeoJson tgj, LineString line, 
                                 double distance, long startTime, long endTime) {
        double x = line.getPointN(0).getCoordinate().getX();
        double y = line.getPointN(0).getCoordinate().getY();
        tgj.writePoint(x, y, 0.0, startTime).comma();
        double d = 0.0;
        for (int i = 1; i < line.getNumPoints(); ++i) {
            d += SphericalDistanceLibrary.distance(line.getPointN(i).getCoordinate(), 
                                                   line.getPointN(i - 1).getCoordinate());
            long time = (long)((double)(endTime - startTime) * (d / distance)) + startTime;
            x = line.getPointN(i).getCoordinate().getX();
            y = line.getPointN(i).getCoordinate().getY();
            tgj.writePoint(x, y, 0.0, time);
            if (i != line.getNumPoints() - 1) {
                tgj.comma();
            }
        }
    }

    /**
     * OTPの検索結果のうち、指定された旅程番号の経路をGeoJson形式のトリップデータとして出力する。
     * @param result OTPの検索結果
     * @param itineraryIndex 旅程番号
     * @throws FileNotFoundException
     */
    public void writeTripData(TOtpResult result, int itineraryIndex) throws FileNotFoundException {
        if (fFirstFlag) {
            fFirstFlag = false;
        } else {
            fTripGeoJson.comma();
        }
        fTripGeoJson.beginTrip();
        GraphPath path = result.getGraphPath(itineraryIndex);
        List<State> states = path.states;
        boolean first = true;
        for (int i = 0; i < states.size(); ++i) {
            State s = states.get(i);
            if (s.getBackEdge() != null) {
                LineString line = s.getBackEdge().getGeometry();
                if (line != null) {
                    double distance = s.getBackEdge().getDistance();
                    long endTime = s.getTimeSeconds() + TIME_DIFFERENCE_UTC_JST_IN_SECONDS; // 時差を修正
                    long startTime = s.getBackState().getTimeSeconds() + TIME_DIFFERENCE_UTC_JST_IN_SECONDS; // 時差を修正
                    if (first) {
                        first = false;                        
                    } else {
                        fTripGeoJson.comma();
                    }
                    writeLineString(fTripGeoJson, line, distance, startTime, endTime);
                }
            }
        }
        fTripGeoJson.endTrip().newline();
    }

    /**
     * ログファイルをクローズする。
     */
    public void close() {
        fTripGeoJson.close();
    }

}
