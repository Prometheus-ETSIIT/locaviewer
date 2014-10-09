
package comunicador;

public class Sensor {
    private static final int PORT_BASE = 4550;
    
    public final String mac;
    public final int id;
    
    public Par posicion;
    
    public Sensor(final String mac, final int id) {
        this.mac = mac;
        this.id  = id;
    }
    
    public int getPort() {
        return PORT_BASE + this.id;
    }
}
