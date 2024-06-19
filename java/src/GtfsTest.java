public class GtfsTest {


   public static void main(String[] args) throws Exception {

       OtpRouting otpRouter = new OtpRouting();
       otpRouter.initializeOtp(null,"data");

       OtpRouting.RoutingResult rr = otpRouter.routingRequest(
               2024,6,10,9,0,
               42.77908192281134, 141.6866373967986,
               31.800646327759004, 130.72020555216076,
               "AIRPLANE");

       System.out.println(rr);



   }
}
