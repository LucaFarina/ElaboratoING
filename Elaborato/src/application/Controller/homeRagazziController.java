package application.Controller;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;

import javax.swing.event.ChangeEvent;

import application.Main;
import application.Model.*;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent ;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML ;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable ;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color ;
import javafx.scene.shape.Rectangle ;
import javafx.stage.Window;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;


public class homeRagazziController implements Initializable{
	Ragazzo user = Main.getUser();
	ArrayList<Vacanza> vacanze = null;
	ArrayList<Vacanza> vacanzePassate = null;
	ArrayList<College> college = null;
	String out = "";
	String separatoreVacanza = "\n\n\n-----------------------------------V A C A N Z A-----------------------------------------------------------------------------------------------\n";
	
	
	//----------------------------------------------------Vacanze Future--------------------------------------------------------------------------------------------------------------------------------
	
	@FXML private DatePicker dateVisualizza;
	@FXML private TextField textVisualizzaDurata;
	@FXML private TextField textVisualizzaCitta;
	@FXML private Button buttFiltroData;
	@FXML private Button buttFiltroDurata;
	@FXML private Button buttFiltroCitta;
	
	@FXML private TextArea areaVacanze;
	
	//---------------------	PRENOTAZIONI-------------------------
	//college
	@FXML private ChoiceBox<String> choicePrenotaCollegeVacanza;
	@FXML private ChoiceBox<String> choicePrenotaCollege;
	@FXML private ChoiceBox<String> choicePrenotaCollegePaga;
	@FXML private ChoiceBox<String> choicePrenotaCollegeStanza;
	@FXML private Button buttPrenotaCollege;
	
	//famiglia
	@FXML private ChoiceBox<String> choicePrenotaFamVacanza;
	@FXML private ChoiceBox<String> choicePrenotaFam;
	@FXML private ChoiceBox<String> choicePrenotaFamPaga;
	@FXML private TextField textNomeAmico;
	@FXML private TextField textEmailAmico;
	@FXML private Button buttPrenotaFam;
	
	
	
	private String[] pagamento = {"Carta di credito", "Bonifico"};
	private String[] tipoStanza = {"Singola", "Doppia"};
	
	
	//----------------------------------------------------Vacanze Passate--------------------------------------------------------------------------------------------------------------------------------

	@FXML private ChoiceBox<String> choiceQuestionarioPrenotazione;
	@FXML private ChoiceBox<Integer> choiceQuestionarioVoto;
	@FXML private TextArea areaCommento;
	@FXML private TextArea areaVacanzePassate;
	@FXML private Button buttSalvaQuestionario;

	private Integer[] voto = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};


	ArrayList<Vacanza> VacanzePassate = new ArrayList<Vacanza>();
	ArrayList<PrenotazioneCollege> prenotazioniCollege = new ArrayList<PrenotazioneCollege>();
	ArrayList<PrenotazioneFam> prenotazioniFam = new ArrayList<PrenotazioneFam>();
	ArrayList<String> prenotazioni = new ArrayList<String>();

	
	//----------------------------------------------------------Profilo-----------------------------------------------------------------------------------------------------------------------
	@FXML private TextField textNomeProf;
	@FXML private TextField textCognomeProf;
	@FXML private TextField textCFProf;
	@FXML private DatePicker dateDdNprof;
	@FXML private TextField textEmailProf;
	@FXML private TextField textNrTelProf;
	@FXML private TextField textIndirizzoProf;



	@FXML private Button buttMod;
	@FXML private Button buttSave;

	@FXML private Tab profilo;
	
	
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		choicePrenotaCollegeVacanza.setOnAction(this::setCollege);
		choicePrenotaFamVacanza.setOnAction(this::setFam);
		setVacanzePassate();
		
		
		choiceQuestionarioVoto.getItems().addAll(voto);
		
		setPage();
	}
	
	
	
	//--------------------------------------------------------------------------------------------------------VACANZE FUTURE--------------------------------------------------------------------------------------------------------------------------------
	
	//metodo per caricare i college corretti relativi alla vacanza selezionata nel box della prenotazione
	private void setCollege(ActionEvent e) {
		choicePrenotaCollege.getItems().clear();
		String vac = choicePrenotaCollegeVacanza.getValue();
		
		 try {
			ArrayList<College> coll = PostreSQLJDBC.getCollegeVacanza(vac);
			String[] codCollege = new String[coll.size()];
			int j = 0;
			for(College i: coll) {
				codCollege[j] = i.getCodice();
				j++;
			}
			choicePrenotaCollege.getItems().addAll(codCollege);
			
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	//metodo per caricare le famiglie corrette nel box della prenotazione
	private void setFam(ActionEvent e) {
		choicePrenotaFam.getItems().clear();
		String vac = choicePrenotaFamVacanza.getValue();
		ArrayList<CapoFamiglia> capo = new ArrayList<CapoFamiglia>();
		ArrayList<Famiglia> fam  = new ArrayList<Famiglia>();
		
		 try {
			PostreSQLJDBC.getFamigliaVacanza(vac, capo, fam);
			String[] codFam = new String[fam.size()];
			int j = 0;
			for(Famiglia i: fam) {
				codFam[j] = i.getCodice();
				j++;
			}
			choicePrenotaFam.getItems().addAll(codFam);
			
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}



	// cancello i valori inseriti nei filtri
	private void resetFiltri() {
		dateVisualizza.setValue(null);
		textVisualizzaDurata.setText(null);
		textVisualizzaCitta.setText(null);
		
	}
	
	//metodo che libera i box di scelta quando applico un nuovo filtro di ricerca
	private void clearBox() {
		choicePrenotaCollegeVacanza.getItems().clear();
		choicePrenotaCollege.getItems().clear();
		choicePrenotaFamVacanza.getItems().clear();
		choicePrenotaFam.getItems().clear();
	}
	
	
	
	// Eseguo la selezione per data
	public void FiltraData(ActionEvent e) throws IOException, ParseException {
		clearBox();
		out = "";
		
		if(college != null) college.clear();
		if(vacanze != null) vacanze.clear();
		
		
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy");  
		LocalDateTime now = LocalDateTime.now();  
		String o = dtf.format(now);
		Date oggi = new SimpleDateFormat("dd-MM-yyyy").parse(o); 
		
		Window owner = buttFiltroData.getScene().getWindow();

		if (dateVisualizza.getValue().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")).isEmpty()) {
			showAlert(Alert.AlertType.ERROR, owner, "Form Error!", "Inserire una data di partenza");
			return;
		}
		String data = dateVisualizza.getValue().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));	
		try {
			vacanze = PostreSQLJDBC.getVacanzaData(data);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String[] codVacanze = new String[vacanze.size()];

		int j = 0;
		for(Vacanza i: vacanze) {
			Date datepartenza = new SimpleDateFormat("dd-MM-yyyy").parse(i.getDataPartenza());  
			
			if(PostreSQLJDBC.isPrenotata(user.getCF(), i.getCodice()) == false) { // non esiste una prenotazione per la vacanza
				if(oggi.before(datepartenza)) {
					codVacanze[j] = i.getCodice();// + " | " + i.getCitta();
					System.out.println(i.toString2());
					System.out.println(getGite(i.getCodice()));
					out += separatoreVacanza;
					out += i.toString2() + getGite(i.getCodice()) + getCollege(i.getCodice()) + getFam(i.getCodice());
					j++;
				}
			}else {
				System.out.println(i.toString2());
				System.out.println(getGite(i.getCodice()));
				out += separatoreVacanza;
				out += i.toString2() + getGite(i.getCodice()) + getCollege(i.getCodice()) + getFam(i.getCodice()) + " \n VACANZA PRENOTATA";
			}
		}
		
		//aggiorno i campi delle prenotazioni in base al filtro applicato � nella ricerca 
		choicePrenotaCollegeVacanza.getItems().addAll(codVacanze);
		choicePrenotaCollegePaga.getItems().addAll(pagamento);
		choicePrenotaCollegeStanza.getItems().addAll(tipoStanza);
		choicePrenotaFamVacanza.getItems().addAll(codVacanze);
		choicePrenotaFamPaga.getItems().addAll(pagamento);
		
		areaVacanze.replaceText(0, areaVacanze.getLength(), out);
		
		resetFiltri();
	}
	
	
	// Eseguo la selezione per durata
	public void FiltraDurata(ActionEvent e) {
		clearBox();
		out = "";
		
		if(college != null) college.clear();
		if(vacanze != null) vacanze.clear();
		
		
		Window owner = buttFiltroDurata.getScene().getWindow();


		if (textVisualizzaDurata.getText().isEmpty()) {
			showAlert(Alert.AlertType.ERROR, owner, "Form Error!", "Inserire la durata preferita");
			return;
		}
		String durata = textVisualizzaDurata.getText();	
		
		try {
			vacanze = PostreSQLJDBC.getVacanzaDurata(durata);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		String[] codVacanze = new String[vacanze.size()];
		
		int j = 0;
		for(Vacanza i: vacanze) {
			if(PostreSQLJDBC.isPrenotata(user.getCF(), i.getCodice()) == false) { // non esiste una prenotazione per la vacanza
				codVacanze[j] = i.getCodice();// + " | " + i.getCitta();
				System.out.println(i.toString2());
				System.out.println(getGite(i.getCodice()));
				out += separatoreVacanza;
				out += i.toString2() + getGite(i.getCodice()) + getCollege(i.getCodice()) + getFam(i.getCodice());
				j++;
			}else {
				System.out.println(i.toString2());
				System.out.println(getGite(i.getCodice()));
				out += separatoreVacanza;
				out += i.toString2() + getGite(i.getCodice()) + getCollege(i.getCodice()) + getFam(i.getCodice()) + " \n VACANZA PRENOTATA";
			}
		}
		
		//aggiorno i campi delle prenotazioni in base al filtro applicato � nella ricerca 
		choicePrenotaCollegeVacanza.getItems().addAll(codVacanze);
		choicePrenotaCollegePaga.getItems().addAll(pagamento);
		choicePrenotaCollegeStanza.getItems().addAll(tipoStanza);
		choicePrenotaFamVacanza.getItems().addAll(codVacanze);
		choicePrenotaFamPaga.getItems().addAll(pagamento);
		
		areaVacanze.replaceText(0, areaVacanze.getLength(), out);
		
		resetFiltri();
	}
	
	
	// Eseguo la selezione per Citt�
	public void FiltraCitta(ActionEvent e) {
		clearBox();
		out = "";
		
		if(college != null) college.clear();
		if(vacanze != null) vacanze.clear();
		
		
		
		Window owner = buttFiltroDurata.getScene().getWindow();


		if (textVisualizzaCitta.getText().isEmpty()) {
			showAlert(Alert.AlertType.ERROR, owner, "Form Error!", "Inserire una Citt�");
			return;
		}
		String citta = textVisualizzaCitta.getText();	

		try {
			vacanze = PostreSQLJDBC.getVacanzaCitta(citta);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		String[] codVacanze = new String[vacanze.size()];
		
		int j = 0;
		for(Vacanza i: vacanze) {
			if(PostreSQLJDBC.isPrenotata(user.getCF(), i.getCodice()) == false) { // non esiste una prenotazione per la vacanza
				codVacanze[j] = i.getCodice();// + " | " + i.getCitta();
				System.out.println(i.toString2());
				System.out.println(getGite(i.getCodice()));
				out += separatoreVacanza;
				out += i.toString2() + getGite(i.getCodice()) + getCollege(i.getCodice()) + getFam(i.getCodice());
				j++;
			}else {
				System.out.println(i.toString2());
				System.out.println(getGite(i.getCodice()));
				out += separatoreVacanza;
				out += i.toString2() + getGite(i.getCodice()) + getCollege(i.getCodice()) + getFam(i.getCodice()) + " \n VACANZA PRENOTATA";
			}
		}
		
		//aggiorno i campi delle prenotazioni in base al filtro applicato � nella ricerca 
		choicePrenotaCollegeVacanza.getItems().addAll(codVacanze);
		choicePrenotaCollegePaga.getItems().addAll(pagamento);
		choicePrenotaCollegeStanza.getItems().addAll(tipoStanza);
		choicePrenotaFamVacanza.getItems().addAll(codVacanze);
		choicePrenotaFamPaga.getItems().addAll(pagamento);
		
		areaVacanze.setText(out);
		
		resetFiltri();
	}

	
	//genero la stringa con tutte le gite della vacanza inserita
	public String getGite(String codice) {
		String g = "\n		--------------------Gite---------------------------------";
		try {
			ArrayList<Gita> gite = PostreSQLJDBC.getGitaVacanza(codice);
			for(Gita i: gite) {
				g += i.toString2();
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//if(g == null) g = "";
		return g;
		
	}
	
	//genero la stringa con tutti i college della vacanza inserita
	public String getCollege(String codice) {
		String g = "";
		try {
			 college = PostreSQLJDBC.getCollegeVacanza(codice);
			
		
			for(College i: college) {
				g = "\n		--------------------College---------------------------------";
				g += i.toString2();
				g += "\n 			------------------------Attivit�---------------------------   " + getAttivita(i.getCodice());
				
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//if(g == null) g = "";
		return g;
		
	}
	
	//genero la stringa con tutte le attivit� della vacanza inserita
	public String getAttivita(String codice) {
		String g = null;
		try {
			ArrayList<Attivita> a = PostreSQLJDBC.getAttivitaCollegeVacanza(codice);
			for(Attivita i: a) {
				g += i.toString2();
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(g == null) g = "";
		return g;
		
	}
	
	//genero la stringa con tutte le famiglie della vacanza inserita
	public String getFam(String codice) {
		String g = "\n		--------------------Famiglie---------------------------------";
		ArrayList<CapoFamiglia> capo = new ArrayList<CapoFamiglia>();
		ArrayList<Famiglia> fam = new ArrayList<Famiglia>();
		try {
			PostreSQLJDBC.getFamigliaVacanza(codice, capo, fam);
			int x = 0;
			for(CapoFamiglia i: capo) {
				g += i.toString2() + fam.get(x).toString2();
				
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//if(g == null) g = "";
		return g;
		
	}
	
	
	
	
	// prenotazione College
	public void PrenotaCollege(ActionEvent e) {
		PrenotazioneCollege prenotazione =  new PrenotazioneCollege(choicePrenotaCollegeVacanza.getValue(), user.getCF(), choicePrenotaCollege.getValue(), choicePrenotaCollegeStanza.getValue(), choicePrenotaCollegePaga.getValue(), false, null);
		
		try {
			PostreSQLJDBC.addPrenotazioneCollege(prenotazione);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	
	
	}
	
	// prenotazione College
	public void PrenotaFam(ActionEvent e) {
		PrenotazioneFam prenotazione =  new PrenotazioneFam(choicePrenotaFamVacanza.getValue(), user.getCF(), choicePrenotaFam.getValue(), textNomeAmico.getText(),  textEmailAmico.getText(), choicePrenotaFamPaga.getValue(), false, null);

		try {
			PostreSQLJDBC.addPrenotazioneFamiglia(prenotazione);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	//--------------------------------------------------------------------------------------------------------VACANZE PASSATE--------------------------------------------------------------------------------------------------------------------------------
	
	
	// metodo che carica tutti i dati relativi alle vacanze passate e setta i campi per l'inserimento del questionario
	public void setVacanzePassate() {
		
		try {
			PostreSQLJDBC.getPrenotazioniPassate(user.getCF(), VacanzePassate, prenotazioniCollege, prenotazioniFam);
			areaVacanzePassate.setText(getPrenotazioneFam(prenotazioniFam) + getPrenotazioneCollege(prenotazioniCollege));
			choiceQuestionarioPrenotazione.getItems().addAll(prenotazioni);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	//metodo che restituisce la stringa con tutte le prenotazioni passate relative a famiglia
	private String getPrenotazioneFam(ArrayList<PrenotazioneFam> prenotazioniFam ) {
		
		String s = "";
		for(PrenotazioneFam i: prenotazioniFam) {
			if(PostreSQLJDBC.isCompilato(i.getCodice()) == false) {
				prenotazioni.add(i.getCodice());
				s += "\n\n----------------------------Vacanza Passata-------------------------";
				s += i.toString() + getVacanza(i.getVacanza()) + "\n Questionario da compilare";
			}else {
				s += "\n\n----------------------------Vacanza Passata-------------------------";
				s += i.toString() + getVacanza(i.getVacanza()) + "\n Questionario compilato !";
			}
		}
	
		return s;
	}
	
	//metodo che restituisce la stringa con tutte le prenotazioni passate relative a famiglia
	private String getPrenotazioneCollege(ArrayList<PrenotazioneCollege> prenotazioniCollege ) {

		String s = "";
		for(PrenotazioneCollege i: prenotazioniCollege) {
			if(PostreSQLJDBC.isCompilato(i.getCodice()) == false) {
				prenotazioni.add(i.getCodice());
				s += "\n\n----------------------------Vacanza Passata-------------------------";
				s += i.toString() + getVacanza(i.getVacanza()) + "\n Questionario da compilare";
			}else {
				s += "\n\n----------------------------Vacanza Passata-------------------------";
				s += i.toString() + getVacanza(i.getVacanza()) + "\n Questionario compilato !";
			}
		}

		return s;
	}

		
	//genera la stringa contenente dati della vacanza trascorsa
	private String getVacanza(String codice) {
		
		for(Vacanza i: VacanzePassate) {
			System.out.println(i.getCodice());
			System.out.println(codice);
			if(i.getCodice().equals(codice)) {
				return "\n Citt�: " + i.getCitta() + "\n Lingua Studiata: " + i.getLingua() + "\n Certificazione: B2\n";
			}
		}
		return "\n";
	}
	
	
	public void salvaQuestionario(ActionEvent e) {
		
		
		Questionario q = new Questionario(choiceQuestionarioPrenotazione.getValue(), choiceQuestionarioVoto.getValue(), areaCommento.getText());
		try {
			PostreSQLJDBC.addQuestionario(q);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		areaCommento.clear();
	}
	
	//--------------------------------------------------------------------------------------------------------------PROFILO-----------------------------------------------------------------------------------------------------------------------
	
	
	
	
	// metodo che consente di modificare i dati
	public void buttMod(ActionEvent e) {
		textNomeProf.setEditable(true);
		textCognomeProf.setEditable(true);
		//textCFProf.setEditable(true);
		dateDdNprof.setEditable(true);
		textEmailProf.setEditable(true);
		textNrTelProf.setEditable(true);
		textIndirizzoProf.setEditable(true);
		buttSave.setDisable(false);
		
	}
	
	
	//metodo che gestisce le modifiche al profilo
	public void salvaModifiche(ActionEvent e) {
		System.out.println(user.toString());
		
		Window owner = buttSave.getScene().getWindow();
		//controllo che i campi siano stati compilati correttamente 
		
		if (textNomeProf.getText().isEmpty()) {
			showAlert(Alert.AlertType.ERROR, owner, "Form Error!", "Inserire un Nome");
			return;
		}
		if (textCognomeProf.getText().isEmpty()) {
			showAlert(Alert.AlertType.ERROR, owner, "Form Error!", "Inserire un Cognome");
			return;
		}
		if (textCFProf.getText().isEmpty() || CFCheck(textCFProf.getText()) == false) {
			showAlert(Alert.AlertType.ERROR, owner, "Form Error!", "Inserire un Cf Valido");
			return;
		}
		if (dateDdNprof.getValue().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")).isEmpty()) {
			showAlert(Alert.AlertType.ERROR, owner, "Form Error!", "Inserire una data di partenza");
			return;
		}
		if (textEmailProf.getText().isEmpty()) {
			showAlert(Alert.AlertType.ERROR, owner, "Form Error!", "Inserire una E-mail");
			return;
		}
		if (textNrTelProf.getText().isEmpty() || textNrTelProf.getText().length() < 10) {
			showAlert(Alert.AlertType.ERROR, owner, "Form Error!", "Inserire un numero di telefono valido ");
			return;
		}
		if (textIndirizzoProf.getText().isEmpty()) {
			showAlert(Alert.AlertType.ERROR, owner, "Form Error!", "Inserire una indirizzo");
			return;
		}
		
		Ragazzo Ragazzo = new Ragazzo(textNomeProf.getText(), textCognomeProf.getText(), null, textEmailProf.getText(), dateDdNprof.getValue().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")), textNrTelProf.getText(), null, null, textIndirizzoProf.getText());
		System.out.println(Ragazzo.toString());
		
		
		
		//esecuzione query tramite jdbc
		try {
			PostreSQLJDBC.UpdateUser(user, Ragazzo);
			updateView(Ragazzo);
			textNomeProf.setEditable(false);
			textCognomeProf.setEditable(false);
			//textCFProf.setEditable(true);
			dateDdNprof.setEditable(false);
			textEmailProf.setEditable(false);
			textNrTelProf.setEditable(false);
			textIndirizzoProf.setEditable(false);
			buttSave.setDisable(true);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
	
	
	
	private static void showAlert(Alert.AlertType alertType, Window owner, String title, String message) {
		Alert alert = new Alert(alertType);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.initOwner(owner);
		alert.show();
	}
	
	private boolean CFCheck(String text) {
		if(text.length() != 16) return false;
		return true;
	}
	

	//metodo che carica i dati dell'utente nei relativi spazi
	public void setPage() {
		textNomeProf.setText(user.getNome());
		textCognomeProf.setText(user.getCognome());
		textCFProf.setText(user.getCF());
		
		//convert String to LocalDate
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		dateDdNprof.setValue(LocalDate.parse(user.getDdN(), formatter));
		
		textEmailProf.setText(user.getEmail());
		textNrTelProf.setText(user.getNrTelefono());
		textIndirizzoProf.setText(user.getIndirizzo());	
	}
	
	
	//metodo che aggiorna i campi dell'utente dopo la modifica
	private void updateView(Ragazzo x) {
		textNomeProf.setText(x.getNome());
		textCognomeProf.setText(x.getCognome());
		
		
		//convert String to LocalDate
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		dateDdNprof.setValue(LocalDate.parse(x.getDdN(), formatter));
		
		textEmailProf.setText(x.getEmail());
		textNrTelProf.setText(x.getNrTelefono());
		textIndirizzoProf.setText(x.getIndirizzo());	
	}



	

	

}
