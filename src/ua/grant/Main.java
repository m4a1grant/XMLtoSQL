package ua.grant;


import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.*;
import java.util.ArrayList;

public class Main {

    private static final String userName = "admin";
    private static final String userPwd = "1234";
    private static final String conectionURL = "jdbc:mysql://localhost:3306/";
    private static final String dbName = "vladymyr_nagornyi";
    private static final String filePath ="data.xml";

    public static void main(String[] args) throws ClassNotFoundException {
        Class.forName("com.mysql.jdbc.Driver");
        createDB();
        prepareDB();
        parseXML();
    }

    private static void createDB (){
        try (Connection connection = DriverManager.getConnection(conectionURL, userName, userPwd)) {
            System.out.println("Подключено успешно");
            Statement statement = connection.createStatement();
            statement.execute("CREATE DATABASE IF NOT EXISTS " + dbName +"");

        } catch (SQLException e){
            System.out.println("Нет доступа к базе данных");

        }
    }

    private static void prepareDB(){
        try (Connection connection = DriverManager.getConnection(conectionURL+dbName, userName, userPwd)) {
            Statement statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS fromXML ( " +
                    "id MEDIUMINT NOT NULL AUTO_INCREMENT PRIMARY KEY , " +
                    "title VARCHAR(50) NOT NULL ," +
                    "artist VARCHAR(50) NOT NULL ," +
                    "country VARCHAR(10) NOT NULL ," +
                    "company VARCHAR(30) NOT NULL ," +
                    "price DOUBLE NOT NULL ," +
                    "year INTEGER NOT NULL " +
                    ")") ;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void addToBD(Connection connection, CD currentCD ){
        try{
            PreparedStatement prepStat = connection.prepareStatement("INSERT INTO fromXML" +
                    "(title, artist, country, company, price, year)" +
                    " VALUES (?,?,?,?,?,?)");
            prepStat.setString(1, currentCD.getTitle());
            prepStat.setString(2, currentCD.getArtist());
            prepStat.setString(3, currentCD.getCountry());
            prepStat.setString(4, currentCD.getCompany());
            prepStat.setDouble(5, currentCD.getPrice());
            prepStat.setInt(6, currentCD.getYear());
            prepStat.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void parseXML(){
        ArrayList<CD> listOfCDs = new ArrayList<>();
        try {
            Connection connection = DriverManager.getConnection(conectionURL+dbName, userName, userPwd);
            FileInputStream input = new FileInputStream("data.xml");
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLEventReader reader = factory.createXMLEventReader(input);
            CD parsedCD = new CD();
            while (reader.hasNext()){
                while (reader.hasNext()) {
                    XMLEvent event = reader.nextEvent();
                    if (event.isStartElement()){
                        String elementName = event.asStartElement().getName().getLocalPart();
                        switch (elementName.toLowerCase()){
                            case "cd":
                                parsedCD = new CD();
                                break;
                            case "title":
                                //возможно брать текс напрямую из елемента плохая идея, т.к getElementText()
                                //делает шаг по дереву документа, который мы не контролируем
                                parsedCD.setTitle(reader.getElementText());
                                break;
                            case "artist":
                                parsedCD.setArtist(reader.getElementText());
                                break;
                            case "country":
                                parsedCD.setCountry(reader.getElementText());
                                break;
                            case "company":
                                parsedCD.setCompany(reader.getElementText());
                                break;
                            case "price":
                                parsedCD.setPrice(Double.parseDouble(reader.getElementText()));
                                break;
                            case "year":
                                parsedCD.setYear(Integer.parseInt(reader.getElementText()));
                                break;
                        }
                    }
                    if (event.isEndElement()){
                        if ("cd".equalsIgnoreCase(event.asEndElement().getName().getLocalPart())){
                            addToBD(connection, parsedCD);
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Файл не найден: " + e.getMessage());
        } catch (XMLStreamException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
