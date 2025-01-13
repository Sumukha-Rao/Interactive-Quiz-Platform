import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Scanner;

import pack.StaticMethod;

public class ViewStats {
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        String table_name = "result";
        String url = "jdbc:mysql://localhost:3306/quiz";
        String user = "root";
        String password = "220313";
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection connection = DriverManager.getConnection(url, user, password);
        Statement statement = connection.createStatement();
        System.out.println("View the results\n1.Highest to lowest\n2.Lowest to highest");
        int choice;
        while (true) {
            System.out.println("Enter your choice:");
            choice = sc.nextInt();
            if (StaticMethod.inRange(choice, 1, 2)) {
                break;
            }
        }
        String query = "";
        if (choice == 1) {
            query = "SELECT * FROM " + table_name + " ORDER BY TotalMarks DESC";
        }
        if (choice == 2) {
            query = "SELECT * FROM " + table_name + " ORDER BY TotalMarks ASC";
        }
        try {
            ResultSet resultSet = statement.executeQuery(query);
            String heading = "Name,Rollno,TotalQuestions,RightAnswers,WrongAnswers,NotAttempted,TotalMarks",
                    value = "";
            while (resultSet.next()) {
                value += resultSet.getString("Name") + "," + resultSet.getString("Rollno") + ","
                        + resultSet.getString("TotalQuestions") + "," + resultSet.getString("RightAnswers") + ","
                        + resultSet.getString("WrongAnswers") + "," + resultSet.getString("NotAttempted") + ","
                        + resultSet.getString("TotalMarks") + "|";
            }
            ProcessBuilder pb = new ProcessBuilder("python", "table.py", heading, value);
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = "";
            String output = "";
            while ((line = reader.readLine()) != null) {
                output += (line + "\n");

            }
            System.out.println(output);
            connection.close();
            sc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
