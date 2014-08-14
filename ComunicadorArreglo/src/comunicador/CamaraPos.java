package comunicador;

public class CamaraPos {
	private Integer posicionX;
	private Integer posicionY;
	private String ID;
	
	public CamaraPos(int x, int y, String id){
		posicionX=x;
		posicionY=y;
		ID=id;
	}
	
	public int getPosX(){
		return posicionX;
	}
	
	public int getPosY(){
		return posicionY;
	}
	
	public String getID(){
		return ID;
	}
}
