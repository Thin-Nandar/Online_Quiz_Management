package server;

import java.rmi.registry.LocateRegistry;
import java.rmi.Naming;

public class AdminServer {
    public static void main(String[] args) {
        try {
            AdminService service = new AdminServiceImpl();

            LocateRegistry.createRegistry(1098);
            Naming.rebind("rmi://localhost:1098/AdminService", service);

            System.out.println("AdminService running on port 1098");

        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
