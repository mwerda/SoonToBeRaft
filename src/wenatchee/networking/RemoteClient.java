package wenatchee.networking;

import wenatchee.protocol.Draft;
import wenatchee.protocol.RaftEntry;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class RemoteClient
{

//    public static void main(String[] args) throws NotBoundException, MalformedURLException, RemoteException {
//        byte[] bytes = new Draft(
//                Draft.DraftType.HEARTBEAT, (byte)0, (byte)1, 2, 2, 3, 3, new RaftEntry[0]
//        ).toByteArray();
//
//        Draft d =  new Draft(
//                Draft.DraftType.HEARTBEAT, (byte)0, (byte)1, 2, 2, 3, 3, new RaftEntry[0]
//        );
//
//        MessengerService service = (MessengerService) Naming.lookup("rmi://localhost:5100/deliverdraft");
//        MessengerService service2 = (MessengerService) Naming.lookup("rmi://localhost:5100/hello");
//        System.out.println(service.sendMessage("Hey server " + service.getClass().getName()));
//        System.out.println(service.deliverDraft(d));
//    }
//
//    public void start() throws NotBoundException, MalformedURLException, RemoteException
//    {
//        byte[] bytes = new Draft(
//                Draft.DraftType.HEARTBEAT, (byte)0, (byte)1, 2, 2, 3, 3, new RaftEntry[0]
//        ).toByteArray();
//
//        Draft d =  new Draft(
//                Draft.DraftType.HEARTBEAT, (byte)0, (byte)1, 2, 2, 3, 3, new RaftEntry[0]
//        );
//
//        MessengerService service = (MessengerService) Naming.lookup("rmi://localhost:5100/deliverdraft");
//        MessengerService service2 = (MessengerService) Naming.lookup("rmi://localhost:5100/hello");
//        System.out.println(service.sendMessage("Hey server " + service.getClass().getName()));
//        System.out.println(service.deliverDraft(d));
//    }
//
//    public void start2(String lastDigit) throws NotBoundException, MalformedURLException, RemoteException
//    {
//        byte[] bytes = new Draft(
//                Draft.DraftType.HEARTBEAT, (byte)0, (byte)1, 2, 2, 3, 3, new RaftEntry[0]
//        ).toByteArray();
//
//        Draft d =  new Draft(
//                Draft.DraftType.HEARTBEAT, (byte)0, (byte)1, 2, 2, 3, 3, new RaftEntry[0]
//        );
//
//        String address = "rmi://10.0.0." + lastDigit + ":5100/deliverdraft";
//        MessengerService service = (MessengerService) Naming.lookup(address);
//        //MessengerService service2 = (MessengerService) Naming.lookup("rmi://169.254.204.245:5100/hello");
//        System.out.println(service.sendMessage("Hey server " + service.getClass().getName()));
//        System.out.println(service.deliverDraft(d));
//    }
    public void deliverDraft(String address, Draft draft) throws NotBoundException, MalformedURLException, RemoteException
    {
//        byte[] bytes = new Draft(
//                Draft.DraftType.HEARTBEAT, (byte)0, (byte)1, 2, 2, 3, 3, new RaftEntry[0]
//        ).toByteArray();
//
//        Draft d =  new Draft(
//                Draft.DraftType.HEARTBEAT, (byte)0, (byte)1, 2, 2, 3, 3, new RaftEntry[0]
//        );

        //String address = "rmi://10.0.0." + lastDigit + ":5100/deliverdraft";
        //MessengerService service = (MessengerService) Naming.lookup(address);
        System.out.println(address);
        MessengerService service = (MessengerService) Naming.lookup(address + "deliverdraft");
        //System.out.println(service.sendMessage("Hey server " + service.getClass().getName()));
        //System.out.println(service2.deliverDraft(d));
        service.deliverDraft(draft);
    }
}
