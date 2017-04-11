// JDBC test By Mike D.
package be.vdab;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Bank {

	private static Scanner scanner;
	private static final String URL = "jdbc:mysql://localhost/bank?useSSL=false";
	private static final String USER = "cursist";
	private static final String PASSWORD = "cursist";
	private static final String SQL_NIEUW_REK = "INSERT INTO rekeningen (RekeningNr) VALUES (?)";
	private static final String SQL_SALDO = "SELECT saldo FROM rekeningen WHERE RekeningNr = ?";
	private static final String SQL_OVER_MIN = "UPDATE rekeningen set Saldo = saldo - ? WHERE RekeningNr=?";
	private static final String SQL_OVER_PLUS = "UPDATE rekeningen set Saldo = saldo + ? WHERE RekeningNr=?";

	public static void main(String[] args) {
		
		scanner = new Scanner(System.in);
		System.out.println("1. Nieuwe rekening\n2. Saldo consulteren \n3.Overschrijven\nTyp je keuze (1,2 of 3)");
		int keuze = scanner.nextInt();
			
		try(Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)){
		
	
		switch (keuze) {
		// Nieuwe rekening maken
		case 1:
			System.out.println("Nieuwe rekening\n---------------");
			System.out.println("Wat is het nieuwe rekening nummer ? (12cijfers)");
			long NrekNR = scanner.nextLong();
			Rekening Nieuw = new Rekening(NrekNR);
			
			if(Nieuw != null){
				try(PreparedStatement statement1 = connection.prepareStatement(SQL_NIEUW_REK)){
					
					statement1.setLong(1, NrekNR);
					statement1.executeUpdate();
					System.out.println("Nieuwe rekening is aangemaakt.");
				}
			}
			break;
		
		//Saldo controleren
		case 2:
			System.out.println("Saldo consulteren\n-----------------");
			System.out.println("Wat is het rekening nummer ? (12cijfers)");
			NrekNR = scanner.nextLong();
			
			try ( PreparedStatement statement2 = connection.prepareStatement(SQL_SALDO)) {

				statement2.setLong(1, NrekNR);
				
					try(ResultSet resultSet = statement2.executeQuery()){
						if (resultSet.next()){
							System.out.println("€" + resultSet.getBigDecimal("saldo"));
						} else if (!resultSet.next()){
							System.err.println("Dit rekening nummer staat niet in onze database");
						}
					}
			}
			break;
		
		//Overschrijving
		case 3:
			System.out.println("Overschrijving\n---------------");
			System.out.println("Vanaf welke rekening wil je een overschrijving doen ? (12cijfers)");
			long VanNrekNR = scanner.nextLong();
			System.out.println("Naar welke rekening wil je de overschrijving doen ? (12cijfers)");
			long NaarNrekNR = scanner.nextLong();
			System.out.println("Hoeveel wilt u overschrijven ?");
			BigDecimal bedrag = scanner.nextBigDecimal();
			boolean fout = false;
			BigDecimal onderNul = BigDecimal.ZERO;
			
			if (VanNrekNR == NaarNrekNR){
				System.err.println("Je kan niet naar je zelf overschrijven.");
			} else {
			
			try ( PreparedStatement statement3 = connection.prepareStatement(SQL_SALDO)) {

					//Controle van start rekening + saldo
					statement3.setLong(1, VanNrekNR);
					try(ResultSet resultSet = statement3.executeQuery()){
						if (resultSet.next()){
							if ((resultSet.getBigDecimal("saldo").compareTo(bedrag) < 0) || bedrag.compareTo(onderNul) < 0){
								System.err.println("Het saldo op de rekening is niet voldoende. Of je hebt een negatief getal ingegeven.");
								fout = true;
							}
						} else if (!resultSet.next()){
							System.err.println("Jouw rekening nummer staat niet in onze database.");
							fout = true;
						}
					}
						
					//Controle van de doel rekening
					statement3.setLong(1, NaarNrekNR);
					try(ResultSet resultSet = statement3.executeQuery()){
						if (!resultSet.next()){	
							System.err.println("Doel rekening nummer staat niet in onze database.");
							fout = true;
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
			}
			break;

		//keuze niet 1, 2 of 3
		default:
			break;
		}
		
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}

