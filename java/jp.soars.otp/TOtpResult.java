package jp.soars.otp;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.List;

import org.locationtech.jts.geom.LineString;
import org.opentripplanner.api.model.Itinerary;
import org.opentripplanner.api.model.Leg;
import org.opentripplanner.api.model.TripPlan;
import org.opentripplanner.common.geometry.SphericalDistanceLibrary;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.spt.GraphPath;

/**
 * OpenTripPlannerによる経路検索結果
 */
public class TOtpResult {

    public enum EOtpStatus {
        SUCCESS, NO_ROUTE, TRIVIAL_PATH
    }

    /** ルーティングのステータス */
    private EOtpStatus fStatus;

    /** グラフパスのリスト */
    private List<GraphPath> fGraphPaths;

    /** 旅行計画 */
    private TripPlan fTripPlan;

    /**
     * コンストラクタ
     */
    public TOtpResult() {
        fStatus = EOtpStatus.NO_ROUTE;
        fGraphPaths = null;
        fTripPlan = null;
    }

    /**
     * コンストラクタ
     * @param request ルーティングリクエスト
     * @param tripPlan 旅行計画
     */
    public TOtpResult(EOtpStatus status, List<GraphPath> graphPaths, TripPlan tripPlan) {
        fGraphPaths = graphPaths;
        fTripPlan = tripPlan;
        fStatus = status;
    }

    /**
     * ルート検索に成功したか？
     * @return true:成功、false:失敗
     */
    public EOtpStatus getStatus() {
        return fStatus;
    }

    /**
     * 検索された旅程の数
     * @return 旅程の数
     */
    public int getNoOfItineraries() {
        return fGraphPaths.size();
    }
    
    /**
     * 出発時刻を返す。
     * @param itineraryIndex 旅程番号
     * @return 出発時刻
     */
    public LocalTime getStartTime(int itineraryIndex) {
        Itinerary plan = fTripPlan.itinerary.get(itineraryIndex);
        return LocalTime.of(plan.startTime.get(Calendar.HOUR_OF_DAY), plan.startTime.get(Calendar.MINUTE));
    }

    /**
     * 到着時刻を返す。
     * @param itineraryIndex 旅程番号
     * @return 到着時刻
     */
    public LocalTime getEndTime(int itineraryIndex) {
        Itinerary plan = fTripPlan.itinerary.get(itineraryIndex);
        return LocalTime.of(plan.endTime.get(Calendar.HOUR_OF_DAY), plan.endTime.get(Calendar.MINUTE));
    }

    /**
     * GraphPathオブジェクトを返す
     * @param itineraryIndex 旅程番号
     * @return GraphPathオブジェクト
     */
    public GraphPath getGraphPath(int itineraryIndex) {
        return fGraphPaths.get(itineraryIndex);
    }

    /**
     * 旅程を返す
     * @param itineraryIndex 旅程番号
     * @return 旅程
     */
    public Itinerary getItinerary(int itineraryIndex) {
        return fTripPlan.itinerary.get(itineraryIndex);
    }

    /**
     * 工程数を返す。
     * @param itineraryIndex 旅程番号
     * @return 工程数
     */
    public int getNoOfLegs(int itineraryIndex) {
        return fTripPlan.itinerary.get(itineraryIndex).legs.size();
    }

    /**
     * 移動手段を返す。
     * @param itineraryIndex 旅程番号
     * @param legIndex 行程番号
     * @return 移動手段
     */
    public String getTraverseModeOfLeg(int itineraryIndex, int legIndex) {
        Leg leg = fTripPlan.itinerary.get(itineraryIndex).legs.get(legIndex);
        return leg.mode;
    }

    /**
     * 行程の形状を返す。形状は、Google MapのPolyline形式でエンコードされている。
     * @param itineraryIndex 旅程番号
     * @param legIndex 工程番号
     * @return 形状
     */
    public String getLegGeometry(int itineraryIndex, int legIndex) {
        Leg leg = fTripPlan.itinerary.get(itineraryIndex).legs.get(legIndex);
        return leg.legGeometry.getPoints();
    }

    /**
     * 行程の出発時刻を返す。
     * @param itineraryIndex 旅程番号
     * @param legIndex 行程番号
     * @return 出発時刻
     */
    public LocalTime getStartTimeOfLeg(int itineraryIndex, int legIndex) {
        Leg leg = fTripPlan.itinerary.get(itineraryIndex).legs.get(legIndex);
        return LocalTime.of(leg.startTime.get(Calendar.HOUR_OF_DAY), leg.startTime.get(Calendar.MINUTE));
    }

    /**
     * 行程の到着時刻を返す。
     * @param itineraryIndex 旅程番号
     * @param legIndex 行程番号
     * @return 到着時刻
     */
    public LocalTime getEndTimeOfLeg(int itineraryIndex, int legIndex) {
        Leg leg = fTripPlan.itinerary.get(itineraryIndex).legs.get(legIndex);
        return LocalTime.of(leg.endTime.get(Calendar.HOUR_OF_DAY), leg.endTime.get(Calendar.MINUTE));
    }

    /**
     * 指定した旅程の指定した時刻における状態を返す。
     * @param itineraryIndex 旅程番号
     * @param hour 時
     * @param minute 分
     * @return 状態
     */
    public TOtpState getState(int itineraryIndex, int hour, int minute) {
        Itinerary plan = fTripPlan.itinerary.get(itineraryIndex);
        int year = plan.startTime.get(Calendar.YEAR);
        int month = plan.startTime.get(Calendar.MONTH) + 1;
        int day = plan.startTime.get(Calendar.DATE);
        LocalDateTime ldt = LocalDateTime.of(year, month, day, hour, minute);
        ZoneId zoneId = ZoneId.of("Asia/Tokyo");
        long targetTime = ldt.atZone(zoneId).toEpochSecond();
        GraphPath path = fGraphPaths.get(itineraryIndex);
        long startTime = path.getStartTime();
        long targetElapsedTime = targetTime - startTime;
        List<State> states = path.states;
        for (State s: states) {
            if (s.getElapsedTimeSeconds() > targetElapsedTime) {
                State prevS = s.getBackState();
                long prevT = prevS.getElapsedTimeSeconds();
                long t = s.getElapsedTimeSeconds();
                double ratio = (double)(targetElapsedTime - prevT) / (double)(t - prevT);
                double d = s.getBackEdge().getDistance() * ratio;
                double distance = 0.0;
                double prevDistance = 0.0;
                LineString line = s.getBackEdge().getGeometry();
                for (int i = 1; i < line.getNumPoints(); ++i) {
                    distance += SphericalDistanceLibrary.distance(line.getPointN(i).getCoordinate(), 
                                                                  line.getPointN(i - 1).getCoordinate());
                    if (distance > d) {
                        double ratio2 = (d - prevDistance) / (distance - prevDistance);
                        double prevX = line.getPointN(i - 1).getCoordinate().getX();
                        double prevY = line.getPointN(i - 1).getCoordinate().getY();
                        double curX = line.getPointN(i).getCoordinate().getX();
                        double curY = line.getPointN(i).getCoordinate().getY();
                        double x = prevX + (curX - prevX) * ratio2; //現在位置のX座標
                        double y = prevY + (curY - prevY) * ratio2; //現在位置のY座標
                        return new TOtpState(s.getBackMode().toString(), y, x);
                    }
                    prevDistance = distance;
                }
                break;
            }
        }
        return null;
    }
}
