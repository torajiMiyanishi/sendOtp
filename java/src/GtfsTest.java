public class GtfsTest {


   public static void main(String[] args) throws Exception {

       OtpRouting otpRouter = new OtpRouting();
       otpRouter.initializeOtp(null,"Y:\\GTFS\\Bus_Japan");

       OtpRouting.RoutingResult rr = otpRouter.routingRequest(
               2024,6,10,9,0,
               35.6738991734632, 139.75100248523478, //
               34.35075274777654, 134.04696677810767,
               "TRANSIT");

       System.out.println(rr.toStringSimple());
       System.out.println(rr);
   }
}
