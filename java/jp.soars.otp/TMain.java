package jp.soars.otp;

import java.io.IOException;

import jp.soars.otp.TOtpResult.EOtpStatus;
import jp.soars.utils.csv.TCCsvData;
import java.time.LocalTime;
import java.util.Random;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.TraverseModeSet;

/**
 * １万エージェントの往復経路探索プログラム
 */
public class TMain {

    public static void main(String[] args) throws IOException {
        long st = System.currentTimeMillis();
//        String pathToZenrin = "C:\\Users\\tora2\\IdeaProjects\\Ondemand-Trans_Mode_in_SOARS\\personal\\master2023\\mf23113_miyanishi\\otp-sample240807-main\\inputData2\\zenrin_building.csv"; // 目的地となる建物名と緯度経度が入っているテーブル
        String pathToPbf = "C:/lab/accessibility/kanto-chiba-roads.osm.pbf"; // "C:\\Users\\tora2\\IdeaProjects\\Ondemand-Trans_Mode_in_SOARS\\personal\\master2023\\mf23113_miyanishi\\otp-sample240807-main\\inputData2\\Ishigakishi.osm.pbf"; // 石垣市のosmデータ
//        String dirToGtfs = "C:\\Users\\tora2\\IdeaProjects\\Ondemand-Trans_Mode_in_SOARS\\personal\\master2023\\mf23113_miyanishi\\otp-sample240807-main\\inputData2\\"; // 石垣市を走行する路線バス・コミュニティバスのGTFSデータが入っているディレクトリ
//        TCCsvData csvZenrin = new TCCsvData(); // 目的地の候補地のcsvを読み込み
//        csvZenrin.readFrom(pathToZenrin);
        TOtpRouter router = new TOtpRouter(pathToPbf, null);



        int noOfItineraries = 1; //検索する旅程の数
        int itineraryIndex = 0; //出力する旅程の番号
        TraverseModeSet modeSet = new TraverseModeSet(TraverseMode.WALK);
        TOtpResult result = router.doIt(noOfItineraries, modeSet, false,
                2024, 9, 4, 6, 0,
                35.673917293201406, 139.91204665925153,
                35.68270209416237, 139.91410659587024
        );
        // シミュレーションのパラメータ 24時間以内のみ考慮しています。
//        int numOfAgents = 10; //エージェント数
//        long seed = 0L;
//        double minLat = 24.340438; //家を生成する領域の緯度の最小値
//        double maxLat = 24.360769; //家を生成する領域の緯度の最大値
//        double minLon = 124.160008; //家を生成する領域の経度の最小値
//        double maxLon = 124.193225; //家を生成する領域の経度の最大値
//        Random random = new Random(seed);
//        TOtpTripLogger logger = new TOtpTripLogger("C:\\Users\\tora2\\IdeaProjects\\Ondemand-Trans_Mode_in_SOARS\\personal\\master2023\\mf23113_miyanishi\\otp-sample240807-main\\logs/trips" + numOfAgents + ".json"); //kepler.gl用のロガー
//        for (int agentId = 0; agentId < numOfAgents; ++agentId){
//            if (agentId % 200 == 0) {
//                System.out.println("agentId is " + agentId + " @" + (System.currentTimeMillis() - st) / 1000 + "秒");
//            }
//            double homeLat = minLat + (maxLat - minLat) * random.nextDouble();
//            double homeLon = minLon + (maxLon - minLon) * random.nextDouble();
//             シミュレーションにおける状態管理変数
//            LocalTime time = LocalTime.of(6, 0);
//            int currentBuildingOfRecordIndex = random.nextInt(csvZenrin.getNoOfRows());
//            double dstLat = csvZenrin.getElementAsDouble(currentBuildingOfRecordIndex, "緯度");
//            double dstLon = csvZenrin.getElementAsDouble(currentBuildingOfRecordIndex, "経度");
//             String dstName = csvZenrin.getElement(currentBuildingOfRecordIndex, "建物名");
//            int year = 2024;
//            int month = 4;
//            int date = 24;
//            int dh = random.nextInt(3); //0～2時間
//            int dm = random.nextInt(60); //0～59分
//            time = time.plusHours(dh); // plusHoursはdmを足したLocalTimeのコピーを返す。
//            time = time.plusMinutes(dm); // plusHoursはdmを足したLocalTimeのコピーを返す。
//            TraverseModeSet modeSet = new TraverseModeSet(TraverseMode.TRANSIT,TraverseMode.WALK,TraverseMode.CAR);
            // TraverseModeSet modeSet = new TraverseModeSet(TraverseMode.CAR);
//            int noOfItineraries = 1; //検索する旅程の数
//            int itineraryIndex = 0; //出力する旅程の番号
//            TOtpResult result = router.doIt(noOfItineraries, modeSet, false,
//                                            year, month, date, time.getHour(), time.getMinute(),
//                                            homeLat, homeLon, dstLat, dstLon);
//            while (result.getStatus() != EOtpStatus.SUCCESS) {
//                System.out.println("Error1!!");
//                // System.out.println(result.getStatus());
//                // System.out.println(homeLat + ", " + homeLon);
//                // System.out.println(dstLat + ", " + dstLon);
//                homeLat = minLat + (maxLat - minLat) * random.nextDouble();
//                homeLon = minLon + (maxLon - minLon) * random.nextDouble();
//                result = router.doIt(noOfItineraries, modeSet, false,
//                                     year, month, date, time.getHour(), time.getMinute(),
//                                     homeLat, homeLon, dstLat, dstLon);
//            }
//            logger.writeTripData(result, itineraryIndex);
//            LocalTime endTripTime = result.getEndTime(itineraryIndex); //到着時刻。旅程の終了時刻。
//            time = endTripTime;
//            dh = random.nextInt(2); //0～1時間
//            dm = random.nextInt(60); //0～59分
//            time = time.plusHours(dh);  // plusHoursはdhを足したLocalTimeのコピーを返す。
//            time = time.plusMinutes(dm);// plusHoursはdmを足したLocalTimeのコピーを返す。
//            result = router.doIt(noOfItineraries, modeSet, false,
//                                 year, month, date, time.getHour(), time.getMinute(),
//                                 dstLat, dstLon, homeLat, homeLon);
//            if (result.getStatus() != EOtpStatus.SUCCESS) {
//                System.out.println("Error2!!");
//                System.exit(1);
//            }
//            logger.writeTripData(result, itineraryIndex); //経路のログを出力
//        }
//        logger.close(); //ロガーをクローズ
//        System.out.println("Time:" + (System.currentTimeMillis() - st) / 1000 + "秒");
        System.out.println(result.getStartTime(itineraryIndex)+"\n"+result.getEndTime(itineraryIndex) + "\n" + result.getLegGeometry(itineraryIndex,0));
    }
}
