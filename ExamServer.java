package server;

import common.ExamInterface;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ExamServer {
    public static void main(String[] args) {
        try {
        	 System.setProperty("java.rmi.server.hostname","127.0.0.1");
 	        LocateRegistry.createRegistry(1099);
 	        ExamServerImpl server = new ExamServerImpl();
 	        Naming.rebind("ExamService", server);

            System.out.println("ExamService running on port 1099");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}










//package server;
//
//import common.ExamInterface;
//import java.rmi.registry.LocateRegistry;
//import java.rmi.Naming;
//
//
//
//public class ExamServer {
//    public static void main(String[] args) {
//        try {
//        	
//            ExamServerImpl examService = new ExamServerImpl();
//            LocateRegistry.createRegistry(1099);
//            Naming.rebind("rmi://localhost:1099/ExamService", examService);
//            System.out.println("ExamService running on port 1099");
//           
//        } catch(Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
