import org.opentripplanner.graph_builder.model.GtfsBundle;
import org.opentripplanner.graph_builder.module.GtfsFeedId;
import org.opentripplanner.graph_builder.module.GtfsModule;
import org.opentripplanner.graph_builder.linking.TransitToStreetNetworkModule;
import org.opentripplanner.graph_builder.module.osm.OpenStreetMapModule;
import org.opentripplanner.routing.error.TrivialPathException;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.api.model.Itinerary;
import org.opentripplanner.api.model.Leg;
import org.opentripplanner.api.model.TripPlan;
import org.opentripplanner.api.model.WalkStep;
import org.opentripplanner.api.resource.GraphPathToTripPlanConverter;
import org.opentripplanner.common.model.GenericLocation;
import org.opentripplanner.openstreetmap.impl.BinaryFileBasedOpenStreetMapProviderImpl;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.TraverseModeSet;
import org.opentripplanner.routing.impl.DefaultStreetVertexIndexFactory;
import org.opentripplanner.routing.impl.GraphPathFinder;
import org.opentripplanner.routing.spt.GraphPath;
import org.opentripplanner.standalone.Router;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;



public class OtpRouting {

   public Graph graph;

   /**
    * OTPのRouterを初期化するクラス
    * @param pathToPbf
    * @param dirToGtfs
    * @throws IOException
    */
   public void initializeOtp(String pathToPbf, String dirToGtfs) throws IOException {
       graph = new Graph();


       // set osm module
       if (pathToPbf != null) {
           HashMap<Class<?>, Object> extra = new HashMap<>();
           BinaryFileBasedOpenStreetMapProviderImpl osmProvider = new BinaryFileBasedOpenStreetMapProviderImpl();
           osmProvider.setPath(new File(pathToPbf));
           SimpleOpenStreetMapContentHandler handler = new SimpleOpenStreetMapContentHandler();
           osmProvider.readOSM(handler);
           OpenStreetMapModule osmModule = new OpenStreetMapModule(Collections.singletonList(osmProvider));
           osmModule.buildGraph(graph, extra);
       }

       // set gtfs module
       List<GtfsBundle> gtfsBundles = null;
       try (final Stream<Path> pathStream = Files.list(Paths.get(dirToGtfs))) {
           gtfsBundles = pathStream.map(Path::toFile).filter(file -> file.getName().toLowerCase().endsWith(".zip"))
                   .map(file -> {
                       GtfsBundle gtfsBundle = new GtfsBundle(file);
                       gtfsBundle.setTransfersTxtDefinesStationPaths(true);
                       String id = file.getName().substring(0, file.getName().length() - 4);
                       gtfsBundle.setFeedId(new GtfsFeedId.Builder().id(id).build());
                       return gtfsBundle;
                   }).collect(Collectors.toList());
       } catch (IOException e) {
           throw new RuntimeException(e);
       }

       GtfsModule gtfsModule = new GtfsModule(gtfsBundles);
       gtfsModule.buildGraph(graph, null);

       TransitToStreetNetworkModule linkModule = new TransitToStreetNetworkModule();
       linkModule.buildGraph(graph, null);

       graph.index(new DefaultStreetVertexIndexFactory());


   }

   /**
    * OTPのRoutingのRequestを投げる関数
    * @param year
    * @param month
    * @param dayOfMonth
    * @param hour
    * @param minute
    * @param o_lat
    * @param o_lon
    * @param d_lat
    * @param d_lon
    * @param traverseModeSetStr
    * @return
    */
   public RoutingResult routingRequest(int year, int month, int dayOfMonth, int hour, int minute,
                                double o_lat, double o_lon, double d_lat, double d_lon,
                                String traverseModeSetStr
                                ){
       LocalDateTime ldt = LocalDateTime.parse("2024-08-01T13:00");
//       LocalDateTime ldt = LocalDateTime.of(year,month,dayOfMonth,hour,minute);
       RoutingRequest routingRequest = new RoutingRequest();
       routingRequest.dateTime = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant()).getTime() / 1000;
       routingRequest.from = new GenericLocation(o_lat, o_lon);
       routingRequest.to = new GenericLocation(d_lat, d_lon);
       routingRequest.setNumItineraries(1);
       routingRequest.setArriveBy(false);
       routingRequest.ignoreRealtimeUpdates = true;
       routingRequest.reverseOptimizing = true;
       routingRequest.onlyTransitTrips = true;

       /* od間のgraphが張れるかTRY */
       try {
           routingRequest.setRoutingContext(graph);
       } catch(TrivialPathException tpe) {
           RoutingResult routingResult = new RoutingResult(false, false, null,null, null,0);
           return routingResult;
       }
       /* TraverseModeをセットしてルート検索をリクエスト */
//       routingRequest.setModes(new TraverseModeSet(traverseModeSetStr));
       routingRequest.setModes(TraverseModeSet.allModes());
       Router router = new Router("OTP_GTFS", graph);
       List<GraphPath> paths = new GraphPathFinder(router).getPaths(routingRequest);
       TripPlan tripPlan;
       try{
            tripPlan = GraphPathToTripPlanConverter.generatePlan(paths, routingRequest);
       }catch(IndexOutOfBoundsException idx){ // ルート検索の結果有効なルートが出力されなかった場合
           RoutingResult routingResult = new RoutingResult(true,false,null, null, null,0);
           return routingResult;
       }

       /* ルーティングが完了し、必要な情報を成型して返す。 */
       Itinerary plan = tripPlan.itinerary.get(0);
       LocalDateTime startTripTime = convertCalendarToLocalDateTime(plan.startTime);
       LocalDateTime endTripTime   = convertCalendarToLocalDateTime(plan.endTime);
       List<Leg> legs              = plan.legs;
       double duration = plan.duration;

       RoutingResult routingResult = new RoutingResult(true,true, startTripTime, endTripTime, legs, duration);
       return routingResult;
   }


   public class RoutingResult {
       private boolean isGeneratedGraph; // グラフが張れたか否か　※張れない=odのどちらかのpointの隣接edgeの探索がうまくいっていない
       private boolean isGeneratedRoute; // グラフが張れた場合に、設定されたTraverseModeで有効なルートが出力されたか否か
       private LocalDateTime startTripTime; // trip全体の開始時刻
       private LocalDateTime endTripTime; // trip全体の終了時刻
       private List<Leg> legs; // tripを構成するlegを格納

       private double duration; //経過時間

       /**
        * OTPのRoutingの結果を格納するクラス
        * @param isGeneratedGraph
        * @param isGeneratedRoute
        * @param startTripTime
        * @param endTripTime
        * @param legs
        */
       public RoutingResult(boolean isGeneratedGraph, boolean isGeneratedRoute, LocalDateTime startTripTime, LocalDateTime endTripTime, List<Leg> legs,double duration) {
           this.isGeneratedGraph   = isGeneratedGraph;
           this.isGeneratedRoute   = isGeneratedRoute;
           this.startTripTime      = startTripTime;
           this.endTripTime        = endTripTime;
           this.legs               = legs;
           this.duration           =duration;
       }
       // getter
       public boolean getIsGeneratedGraph() { return isGeneratedGraph; }
       public boolean getIsGeneratedRoute() { return isGeneratedRoute; }
       public LocalDateTime getStartTripTime() {return startTripTime;}
       public LocalDateTime getEndTripTime() {return endTripTime;}
       public List<Leg> getLegs() {return legs;}

       public double getduration(){return duration;}

       public String toString(){
           String outStr = "";
           for (Leg leg: legs){
               outStr += "[mode]:"+leg.mode +"[start]:"+convertCalendarToLocalDateTime(leg.startTime) +"[end]:"+convertCalendarToLocalDateTime(leg.endTime)+ "[routeName]" + leg.routeShortName +"[tripName]" + leg.tripShortName + "[o_sta]" + leg.from.name + "[d_sta]" + leg.to.name + "\n";
           }
           return outStr;
       }
       public String toStringSimple(){
           return "[isGeneratedGraph] " + isGeneratedGraph +" [isGeneratedRoute] " + isGeneratedRoute + " [startTripTime] " + startTripTime + " [endTripTime] " + endTripTime + " [legs] " + legs;
       }
   }


   public static void convertLegToTripSteps(List<Leg> legs){
   }

   public class TripStep{
       private String fromVertexOsmId;
       private String toVertexOsmId;
       private LocalDateTime startTripStepTime;
       private double durationOfTripStep;
       private String geometriesStr;

       public TripStep(String fromVertexOsmId, String toVertexOsmId, LocalDateTime startTripStepTime, double durationOfTripStep, String geometriesStr){
           this.fromVertexOsmId    = fromVertexOsmId;
           this.toVertexOsmId      = toVertexOsmId;
           this.startTripStepTime  = startTripStepTime;
           this.durationOfTripStep = durationOfTripStep;
           this.geometriesStr      = geometriesStr;
       }
   }

   // 二地点間の直線距離計算 ※近すぎる二点のルーティングがエラーの基となるため
   public static double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
       double R = 6371.01; // 地球の半径（キロメートル）
       // 緯度経度をラジアンに変換
       double dLat = Math.toRadians(lat2 - lat1);
       double dLon = Math.toRadians(lon2 - lon1);
       lat1 = Math.toRadians(lat1);
       lat2 = Math.toRadians(lat2);

       // ハーバーサイン公式による距離の計算
       double a = Math.pow(Math.sin(dLat / 2), 2)
               + Math.cos(lat1) * Math.cos(lat2)
               * Math.pow(Math.sin(dLon / 2), 2);
       double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
       double distance = R * c; // 結果をキロメートル単位で出力

       return distance;
   }

   /**
    * Calendar型からLocalDataTime型への変換
    * @param dateTimeCalendar
    * @return
    */
   public static LocalDateTime convertCalendarToLocalDateTime(Calendar dateTimeCalendar){
       int year = dateTimeCalendar.get(Calendar.YEAR);
       int month = dateTimeCalendar.get(Calendar.MONTH);
       int dayOfMonth = dateTimeCalendar.get(Calendar.DAY_OF_MONTH );
       int hour = dateTimeCalendar.get(Calendar.HOUR_OF_DAY);
       int minute = dateTimeCalendar.get(Calendar.MINUTE);
       int second = dateTimeCalendar.get(Calendar.SECOND);

       return LocalDateTime.of(year,month,dayOfMonth,hour,minute,second);
   }

   /**
    * osmIdの余計な文字を削除
    * @param labelOrName
    * @return
    */
   public static String extractOsmId(String labelOrName){
       return labelOrName.replace("osm:node:","");
   }

}



