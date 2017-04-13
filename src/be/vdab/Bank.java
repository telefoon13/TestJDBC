package be.vdab;

/* JDBC test By Mike D. V1.5*/

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Bank {

	private static Scanner scanner;
	private static int keuze;
	private static final String DB = "jdbc:mysql://localhost/";
	private static final String DB_NAME = "bank";
	private static final String DB_SECURE = "?useSSL=false";
	private static final String USER = "root";
	private static final String PASSWORD = "vdab";
	private static final String SQL_NIEUW_REK = "INSERT INTO rekeningen (RekeningNr) VALUES (?)";
	private static final String SQL_SALDO = "SELECT saldo FROM rekeningen WHERE RekeningNr = ?";
	private static final String SQL_OVER_MIN = "UPDATE rekeningen set saldo = saldo - ? WHERE RekeningNr=?";
	private static final String SQL_OVER_PLUS = "UPDATE rekeningen set saldo = saldo + ? WHERE RekeningNr=?";
	private static final String SQL_CREATE_DB = "CREATE DATABASE " + DB_NAME;
	private static final String SQL_CREATE_TB = "CREATE TABLE rekeningen "
											+ "(ID INTEGER(5) auto_increment primary key not null, RekeningNr BIGINT not null, saldo DECIMAL(12,2) not null DEFAULT 0)";

	public static void main(String[] args) {
		
		System.out.println("-------------------------\n|   TEST JDBC MIKE D.   |\n-------------------------");
		
		scanner = new Scanner(System.in);
		toonMenu();
			
		while (keuze != 0){
		
		switch (keuze) {
		// Nieuwe rekening maken
		case 1:
			try(Connection connection = DriverManager.getConnection(DB + DB_NAME + DB_SECURE, USER, PASSWORD)){
				
				subMenu("Nieuwe rekening");
				rekVragen();
				long NrekNR = scanner.nextLong();
				
				if (NrekNR < 100_000_000_000L || NrekNR > 999_999_999_999L){
					fout("Rekening nummer moet een getal met 12 cijfers zijn");
				} else if (!chekRekNr(NrekNR)) {
					fout("Foutief rekening nummer");
				} else {
					
					try(PreparedStatement statement1 = connection.prepareStatement(SQL_NIEUW_REK)){
					
						statement1.setLong(1, NrekNR);
						statement1.executeUpdate();
						System.out.println("Nieuwe rekening is aangemaakt.");
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
				System.exit(0);
			}
			toonMenu();
			break;
		
		//Saldo controleren
		case 2:
			try(Connection connection = DriverManager.getConnection(DB + DB_NAME + DB_SECURE, USER, PASSWORD)){
				subMenu("Saldo consulteren");
				rekVragen();
				long NrekNR = scanner.nextLong();
			
				try ( PreparedStatement statement2 = connection.prepareStatement(SQL_SALDO)) {

					statement2.setLong(1, NrekNR);
				
						try(ResultSet resultSet = statement2.executeQuery()){
							if (resultSet.next()){
								System.out.println("€" + resultSet.getBigDecimal("saldo"));
							} else if (!resultSet.next()){
								fout("Dit rekening nummer staat niet in onze database");
							}
						}
				}
			} catch (SQLException e) {
				e.printStackTrace();
				System.exit(0);
			}
			toonMenu();
			break;
		
		//Overschrijving
		case 3:
			try(Connection connection = DriverManager.getConnection(DB + DB_NAME + DB_SECURE, USER, PASSWORD)){
				subMenu("Overschrijving");
				
				rekVragen("Van : ");
				long VanNrekNR = scanner.nextLong();
				rekVragen("Naar : ");
				long NaarNrekNR = scanner.nextLong();
				System.out.println("Hoeveel wilt u overschrijven ?");
				BigDecimal bedrag = scanner.nextBigDecimal();
				
				boolean fout = false;
				BigDecimal onderNul = BigDecimal.ZERO;
			
				if (VanNrekNR == NaarNrekNR){
					fout("Je kan niet naar je zelf overschrijven.");
				} else {
			
					try ( PreparedStatement statement3 = connection.prepareStatement(SQL_SALDO)) {

						//Controle van start rekening + saldo
						statement3.setLong(1, VanNrekNR);
						try(ResultSet resultSet = statement3.executeQuery()){
							if (resultSet.next()){
								if ((resultSet.getBigDecimal("saldo").compareTo(bedrag) < 0) || bedrag.compareTo(onderNul) < 0){
									fout("Het saldo op de rekening is niet voldoende. Of je hebt een negatief getal ingegeven.");
									fout = true;
								}
							} else if (!resultSet.next()){
								fout("Jouw rekening nummer staat niet in onze database.");
								fout = true;
							}
						}
						
						//Controle van de doel rekening
						statement3.setLong(1, NaarNrekNR);
						try(ResultSet resultSet = statement3.executeQuery()){
							if (!resultSet.next()){	
								fout("Doel rekening nummer staat niet in onze database.");
								fout = true;
							}
						}
					}
					
						//De overschrijving zelf
						if (!fout){
							try(
							PreparedStatement statement4 = connection.prepareStatement(SQL_OVER_MIN);
							PreparedStatement statement5 = connection.prepareStatement(SQL_OVER_PLUS)
							){
								connection.setAutoCommit(false);
								statement4.setBigDecimal(1, bedrag);
								statement5.setBigDecimal(1, bedrag);
								statement4.setLong(2, VanNrekNR); 
								statement5.setLong(2, NaarNrekNR);
								statement4.executeUpdate();
								statement5.executeUpdate();
								connection.commit();
								System.out.println("Er is €" + bedrag + " overgeschreven");
							}
						}	
			}
				
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(0);
		}
			toonMenu();
			break;
			
		//Database en tabellen maken
		case 4:
			subMenu("DataBase Maken");
			
			//Database maken
			try(
				Connection connection2 = DriverManager.getConnection(DB + DB_SECURE, USER, PASSWORD);
				PreparedStatement statement6 = connection2.prepareStatement(SQL_CREATE_DB)
			){
				statement6.executeUpdate();
				System.out.println("Database aangemaakt");
			} catch (SQLException e) {
				if (e.getErrorCode() == 1007){
					fout("Database is al gemaakt");
				} else {
					e.printStackTrace();
					System.exit(0);
				}
			}
			
			//Tabellen maken
			try(
				Connection connection = DriverManager.getConnection(DB + DB_NAME + DB_SECURE, USER, PASSWORD);
				PreparedStatement statement7 = connection.prepareStatement(SQL_CREATE_TB)
			){
				statement7.executeUpdate();
				System.out.println("Tabellen aangemaakt");
			} catch (SQLException e) {
				if (e.getErrorCode() == 1050){
					fout("Tabbellen zijn al gemaakt.");
				} else {
					e.printStackTrace();
					System.exit(0);
				}
			}
			
			toonMenu();
			break;

		//keuze niet 1, 2, 4, 0
		default:
			fout("Je moet binnen de keuze mogelijkheden bijven.");
			toonMenu();
			break;
		}
	
		}
	}
	
	//Methodes
	
	//Hoofd Menu
	public static void toonMenu() {
		System.out.println(
				  "\n-------------------------\n"
				+ "| 1. Nieuwe rekening    |\n"
				+ "| 2. Saldo consulteren  |\n"
				+ "| 3. Overschrijven      |\n"
				+ "| 4. Database maken     |\n"
				+ "| 0. Stoppen            |\n"
				+ "-------------------------"
				);
		keuze = scanner.nextInt();
	}
	
	//Sub menu
	private static void subMenu(String titel) {
		System.out.println(titel + "\n-------------------------");
	}
	
	//Rekening nr vragen
	private static void rekVragen(String extra) {
		System.out.println(extra + "Geef rekening nummer ? (12cijfers)");
	}
	private static void rekVragen() {
		System.out.println("Geef rekening nummer ? (12cijfers)");
	}
	
	//Fouten
	private static void fout(String fout) {
		System.err.println( fout );
	}
	
	private static boolean chekRekNr(Long reknr) {
		long eerste10 = reknr/100;
		long laaste2 = reknr % 10;
		long controle = eerste10 % 97;
		return controle == laaste2;
	}
}

