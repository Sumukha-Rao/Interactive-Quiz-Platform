import java.net.Socket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Scanner;
import pack.*;

public class QuizClient {
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 12345);
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            System.out.println("Welcome to the Quiz");
            System.out.println("Enter your name:");
            String name = sc.nextLine();
            System.out.println("Enter your roll number:");
            String rollno = sc.nextLine();
            dos.writeUTF(name);
            dos.flush();
            dos.writeUTF(rollno);
            dos.flush();
            int opShuffled = dis.readInt();
            int rowCount = Integer.parseInt(dis.readUTF());
            Question[] question = new Question[rowCount];
            WithAnswer[] withAnswer = new WithAnswer[rowCount];
            for (int i = 0; i < rowCount; i++) {
                question[i] = (Question) ois.readObject();
            }
            for (int i = 0; i < rowCount; i++) {
                withAnswer[i] = new WithAnswer(question[i], 0);
            }
            for (int i = 0; i < rowCount; i = i % rowCount) {
                System.out.println();
                int answer = displayQuestion(question[i], opShuffled, withAnswer, i);
                if (answer == 5) {
                    break;
                } else if (answer == 6) {
                    i -= 1;
                } else if (answer == 7) {
                    i++;
                }
                i = i < 0 ? rowCount - 1 : i;
            }
            dos.writeUTF("submit");
            dos.flush();
            int ans[] = new int[rowCount];
            for (int i = 0; i < rowCount; i++) {
                ans[i] = withAnswer[i].answer;
            }
            oos.writeObject(ans);
            oos.flush();
            WithAnswer[] correctAnswers = new WithAnswer[rowCount];
            for (int i = 0; i < rowCount; i++) {
                correctAnswers[i] = (WithAnswer) ois.readObject();
            }
            for (int i = 0; i < rowCount; i++) {
                System.out.println("Question " + (i + 1) + ": " + question[i].question);
                System.out.println("Correct answer: " + correctAnswers[i].answer);
                System.out.println("Your answer: " + withAnswer[i].answer);
            }
            String scoreCard = dis.readUTF();
            System.out.println(scoreCard);
            socket.close();
            sc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static int displayQuestion(Question q, int opShuffled, WithAnswer[] withAnswer, int index) throws Exception {
        System.out.println((index + 1) + "." + "Question: " + q.question);
        if (opShuffled == 0) {
            System.out.println("1. " + q.op1);
            System.out.println("2. " + q.op2);
            System.out.println("3. " + q.op3);
            System.out.println("4. " + q.op4);
            System.out.println("Your selected : " + withAnswer[index].answer);
            System.out.println("0.Skip question 5.Submit answers 6.Prev question 7.Next question");
            System.out.println("Enter your answer:");
            int answer = 0;
            while (true) {
                answer = sc.nextInt();
                if (StaticMethod.inRange(answer, 0, 7)) {
                    if (StaticMethod.inRange(answer, 0, 4)) {
                        withAnswer[index].answer = answer;
                        return 7;
                    } else
                        return answer;
                } else {
                    System.out.println("Invalid input. Please enter a number between 0 and 7");
                    continue;
                }
            }
        } else {
            int[] opArray = StaticMethod.ShuffledArray(0, 4);
            String[] options = { q.op1, q.op2, q.op3, q.op4 };
            for (int i = 0; i < 4; i++) {
                System.out.println((i + 1) + ". " + options[opArray[i]]);
            }
            String selected = withAnswer[index].answer == 0 ? "0" : options[withAnswer[index].answer - 1];
            System.out.println("Your selected : " + selected);
            System.out.println("0.Skip question 5.Submit answers 6.Prev question 7.Next question");
            System.out.println("Enter your answer:");
            int answer = 0;
            while (true) {
                answer = sc.nextInt();
                if (StaticMethod.inRange(answer, 0, 7)) {
                    if (StaticMethod.inRange(answer, 0, 4)) {
                        withAnswer[index].answer = opArray[answer - 1] + 1;
                        return 7;
                    } else
                        return answer;
                } else {
                    System.out.println("Invalid input. Please enter a number between 0 and 7");
                    continue;
                }
            }

        }
    }
}
