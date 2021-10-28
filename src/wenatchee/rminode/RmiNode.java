package wenatchee.rminode;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class RmiNode {

    String config;
    int index;
    Identity identity;
    Identity[] peers;

    public class Identity {
        String ip;
        int port;
        int id;
        String remoteAddress;

        public Identity(String address, int port, int id) {
            this.ip = address;
            this.port = port;
            this.id = id;
            this.remoteAddress = "rmi://" + address + ":" + String.valueOf(port) + "/";
        }

    public RmiNode(String config, int index) throws FileNotFoundException {
        ArrayList<Identity> allIdentities = getAllIdentities(config);


    }

    public ArrayList<Identity> getAllIdentities(String config) throws FileNotFoundException {
        ArrayList<Identity> identities = new ArrayList<>();
        File configFile = new File(config);
        Scanner inputStream = new Scanner(configFile);
        while(inputStream.hasNext())
        {
            String[] line = inputStream.next().split(",");
            identities.add(new Identity(line[0], Integer.parseInt(line[1]), (byte) Integer.parseInt(line[2])));
        }

        inputStream.close();
        return identities;
    }
};
