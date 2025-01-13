import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.io.IOException;
import pack.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class QuizServer {
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.println("Welcome to Quiz server");
        System.out.println(
                "Ensure you have data in \n| Slno. | Question | Option1 |Option2 |Option3 | Option4 | Answer| \nformat");
        init();

        String url = "jdbc:mysql://localhost:3306/quiz";
        String user = "root";
        String password = "220313";
        String query = "SELECT * FROM quiz_data";
        int rowCount = 0;
        Question[] question = new Question[0];
        WithAnswer[] withAnswer = new WithAnswer[0];
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(url, user, password);
            Statement stmt = connection.createStatement();
            stmt.executeQuery(
                    "CREATE TABLE IF NOT EXISTS result ( Name text, Rollno text, TotalQuestions text, RightAnswers text, WrongAnswers text, NotAttempted text, TotalMarks text );");
            PreparedStatement statement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            ResultSet resultSet = statement.executeQuery();
            resultSet.last();
            rowCount = resultSet.getRow();
            resultSet.beforeFirst();
            question = new Question[rowCount];
            withAnswer = new WithAnswer[rowCount];
            for (int i = 0; resultSet.next(); i++) {
                int slno = Integer.valueOf(resultSet.getString("Slno."));
                String questions = resultSet.getString("Question");
                String op1 = resultSet.getString("Option1");
                String op2 = resultSet.getString("Option2");
                String op3 = resultSet.getString("Option3");
                String op4 = resultSet.getString("Option4");
                int answer = Integer.valueOf(resultSet.getString("Answer"));
                question[i] = new Question(slno, questions, op1, op2, op3, op4);
                withAnswer[i] = new WithAnswer(question[i], answer);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        int qShuffled = 0, opShuffled = 0;
        try {
            System.out.println("Enter the marks for right answer : ");
            int markForRightAnswer = sc.nextInt();
            if (markForRightAnswer <= 0) {
                System.out.println("Marks for right answer should be greater than 0\n Marks set to 1");
                markForRightAnswer = 1;
            }
            System.out.println("Enter the marks for wrong answer : ");
            int markForWrongAnswer = sc.nextInt();
            if (markForWrongAnswer <= 0)
                markForWrongAnswer *= -1;
            Mark.markForWrongAnswer = markForWrongAnswer;
            Mark.markForRightAnswer = markForRightAnswer;
            System.out.println("Do you want to shuffle the questions? (y/n)");
            while (true) {
                String s = sc.next();
                qShuffled = StaticMethod.yesOrNo(s);
                if (qShuffled == -1) {
                    System.out.println("Invalid input. Please enter y or n");
                    continue;
                } else {
                    break;
                }
            }
            System.out.println("Do you want to shuffle the options? (y/n)");
            while (true) {
                String s = sc.next();
                opShuffled = StaticMethod.yesOrNo(s);
                if (opShuffled == -1) {
                    System.out.println("Invalid input. Please enter y or n");
                    continue;
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("Server is listening on port 12345...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                new ClientHandler(clientSocket, question, withAnswer, rowCount, qShuffled, opShuffled).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        sc.close();
    }

    static void init() throws Exception {
        String pythonScriptPath = "xlToDb.py";
        int res = 0;
        while (true) {
            System.out.println();
            System.out.print("Enter Excel file path  :");
            String inputString = sc.nextLine();
            // System.out.println(inputString);
            try {
                ProcessBuilder pb = new ProcessBuilder("python", pythonScriptPath, inputString);
                Process process = pb.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
                int exitCode = process.waitFor();
                res = exitCode;
                System.out.println("Python script exited with code: " + exitCode);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            if (res == 0) {
                break;
            }
        }
    }
}

class ClientHandler extends Thread {
    private Socket clientSocket;
    Question[] UnShuffledQuestion;
    Question[] question;
    WithAnswer[] withAnswer;
    WithAnswer[] UnShuffledWithAnswer;
    int rowCount, opShuffled = 0;

    public ClientHandler(Socket clientSocket, Question[] question, WithAnswer[] withAnswer, int rowCount, int qShuffled,
            int opShuffled) {
        this.clientSocket = clientSocket;
        this.UnShuffledQuestion = question;
        this.UnShuffledWithAnswer = withAnswer;
        this.rowCount = rowCount;
        this.opShuffled = opShuffled;
        if (qShuffled == 1) {
            int[] shuffledArray = StaticMethod.ShuffledArray(0, rowCount);
            this.question = new Question[rowCount];
            this.withAnswer = new WithAnswer[rowCount];
            for (int i = 0; i < rowCount; i++) {
                this.question[i] = UnShuffledQuestion[shuffledArray[i]];
                this.withAnswer[i] = UnShuffledWithAnswer[shuffledArray[i]];
            }
        } else {
            this.question = UnShuffledQuestion;
            this.withAnswer = UnShuffledWithAnswer;
        }
    }

    @Override
    public void run() {
        try (
                ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
                ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
                DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());) {
            System.out.println("Client " + clientSocket.getInetAddress() + " connected.");
            String ClientName;
            String ClientRollno;
            ClientName = dis.readUTF();
            ClientRollno = dis.readUTF();
            dos.writeInt(opShuffled);
            dos.flush();
            dos.writeUTF(String.valueOf(rowCount));
            dos.flush();
            for (int i = 0; i < rowCount; i++) {
                oos.writeObject(question[i]);
                oos.flush();
            }
            while (true) {
                String s = dis.readUTF();
                if (s.equalsIgnoreCase("submit")) {
                    break;
                }
            }
            int clientAns[] = new int[rowCount];
            clientAns = (int[]) ois.readObject();

            for (int i = 0; i < rowCount; i++) {
                oos.writeObject(withAnswer[i]);
                oos.flush();
            }
            int rightAns[] = new int[rowCount];
            for (int i = 0; i < rowCount; i++) {
                rightAns[i] = withAnswer[i].answer;
            }
            Result result = new Result(ClientName, ClientRollno, rightAns, clientAns);
            result.updateInDatabase();
            String scoreCard = result.scoreCard();
            dos.writeUTF(scoreCard);
            dos.flush();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
                System.out.println("Client " + clientSocket.getInetAddress() + " disconnected.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

class Mark {
    public static int markForRightAnswer = 1;
    public static int markForWrongAnswer = 0;

    Mark(int markForRightAnswer, int markForWrongAnswer) {
        Mark.markForRightAnswer = markForRightAnswer;
        Mark.markForWrongAnswer = markForWrongAnswer;
    }
}

class Result {
    int right = 0, wrong = 0, notAttempted = 0;
    String ClientName, ClientRollno;
    int[] rightAns, clientAns;

    Result(String ClientName, String ClientRollno, int rightAns[], int clientAns[]) {
        this.ClientName = ClientName;
        this.ClientRollno = ClientRollno;
        this.rightAns = rightAns;
        this.clientAns = clientAns;
    }

    int calculateMark() {
        right = wrong = notAttempted = 0;
        int mark = 0;
        for (int i = 0; i < rightAns.length; i++) {
            if (clientAns[i] == 0) {
                notAttempted++;
                continue;
            }
            if (rightAns[i] == clientAns[i]) {
                mark += Mark.markForRightAnswer;
                right++;
            } else {
                mark -= Mark.markForWrongAnswer;
                wrong++;
            }
        }
        return mark;

    }

    String scoreCard() {
        String marksCard = "";
        String heading = "Name,Rollno,TotalQuestions,RightAnswers,WrongAnswers,NotAttempted,TotalMarks";
        String value = ClientName + "," + ClientRollno + "," + rightAns.length + "," + right + "," + wrong + ","
                + notAttempted + "," + calculateMark();
        try {
            ProcessBuilder pb = new ProcessBuilder("python", "table.py", heading, value);
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = "";
            while ((line = reader.readLine()) != null) {
                marksCard += (line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return marksCard;
    }

    int updateInDatabase() {
        String url = "jdbc:mysql://localhost:3306/quiz";
        String user = "root";
        String password = "220313";
        int totalMark = calculateMark();
        String query = "INSERT INTO result VALUES('" + ClientName + "','" + ClientRollno + "'," + clientAns.length
                + " ," + right + "," + wrong + "," + notAttempted + "," + totalMark + ")";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(url, user, password);
            Statement statement = connection.createStatement();
            statement.executeUpdate(query);
            connection.close();
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
