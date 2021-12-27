package application.Model;

public class College {
	private String Nome = new String();
	private String Indirizzo = new String();
	private int Vacanza;
	
	public String getNome() {
		return this.Nome;
	}
	public void setNome(String nome) {
		this.Nome = nome;
	}
	public String getIndirizzo() {
		return this.Indirizzo;
	}
	public void setIndirizzo(String indirizzo) {
		this.Indirizzo = indirizzo;
	}
	public int getVacanza() {
		return this.Vacanza;
	}
	public void setVacanza(int vacanza) {
		//@TODO
		//CONTROLLO VALIDITA' DEL CODICE INSERITO
		this.Vacanza = vacanza;
	}
	
	public College(String nome, String indirizzo, int vacanza) {
		this.setNome(nome);
		this.setIndirizzo(indirizzo);
		this.setVacanza(vacanza);
	}
	
}
