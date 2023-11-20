import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class MathQuizApplication {
    private static int remainingTime;
    private static boolean timerExpired = false;
    private static Map<String, Integer> operationCounts = new HashMap<>();
    private static Map<String, Integer> correctCounts = new HashMap<>();
    private static List<String> wrongQuestions = new ArrayList<>();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to Math Quiz!");
        System.out.print("Enter your name: ");
        String studentName = scanner.nextLine();

        boolean extendedTime = false;
        boolean validInput = false;
        while (!validInput) {
            System.out.println("Do you need extended time? (yes/no): ");
            String extendedTimeInput = scanner.nextLine().toLowerCase();
            if (extendedTimeInput.equals("yes")) {
                System.out.print("Enter the password for extended time: ");
                String password = scanner.nextLine();
                if (password.equalsIgnoreCase("novus")) {
                    extendedTime = true;
                    validInput = true;
                } else {
                    System.out.println("Incorrect password. Please try again.");
                }
            } else if (extendedTimeInput.equals("no")) {
                validInput = true;
            } else {
                System.out.println("Invalid input. Please enter 'yes' or 'no'.");
            }
        }

        if (extendedTime) {
            remainingTime = 60;
        } else {
            remainingTime = 45;
        }

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                if (remainingTime == 0) {
                    timerExpired = true;
                    timer.cancel();
                } else {
                    remainingTime--;
                }
            }
        }, 1000, 60000); // Decrease remaining time every minute

        int totalQuestions = 20;
        int correctAnswers = 0;

        Random random = new Random();

        for (int i = 1; i <= totalQuestions; i++) {
            if (timerExpired) {
                System.out.println("Time's up! The quiz has ended.");
                break;
            }

            int num1 = random.nextInt(50) + 1;
            int num2 = random.nextInt(50) + 1;

            String question = "";
            double correctResult = 0.0;
            String questionType = "";

            if (i <= 10) {
                int operator = random.nextInt(4); // 0: addition, 1: subtraction, 2: multiplication, 3: division
                char operatorChar = ' ';

                switch (operator) {
                    case 0:
                        operatorChar = '+';
                        correctResult = num1 + num2;
                        questionType = "Addition";
                        break;
                    case 1:
                        operatorChar = '-';
                        if (num1 < num2) {
                            int temp = num1;
                            num1 = num2;
                            num2 = temp;
                        }
                        correctResult = num1 - num2;
                        questionType = "Subtraction";
                        break;
                    case 2:
                        operatorChar = '*';
                        correctResult = num1 * num2;
                        questionType = "Multiplication";
                        break;
                    case 3:
                        operatorChar = '/';
                        correctResult = (double) num1 / num2; // Allow decimals for division
                        questionType = "Division";
                        break;
                }

                question = "Question " + i + " (" + questionType + "): " + num1 + " " + operatorChar + " " + num2;
            } else {
                int operator1 = random.nextInt(2); // 0: addition, 1: subtraction
                int operator2 = random.nextInt(2) + 2; // 2: multiplication, 3: division
                char operatorChar1 = (operator1 == 0) ? '+' : '-';
                char operatorChar2 = (operator2 == 2) ? '*' : '/';

                correctResult = operator1 == 0 ? num1 + num2 : num1 - num2;
                correctResult = operator2 == 2 ? correctResult * num2 : correctResult / num2;

                question = "Question " + i + " (" + operatorChar1 + " and " + operatorChar2 + "): " +
                        num1 + " " + operatorChar1 + " " + num2 + " " + operatorChar2 + " " + num2;
            }

            // Display question and get user answer
            boolean validAnswer = false;
            double userAnswer = 0.0;
            while (!validAnswer) {
                try {
                    System.out.print(question + " = ");
                    userAnswer = scanner.nextDouble();
                    validAnswer = true;
                } catch (Exception e) {
                    System.out.println("Invalid input. Please enter a number.");
                    scanner.nextLine(); // Consume the invalid input
                }
            }

            // Compare user answer with correct result
            if (Math.abs(userAnswer - correctResult) < 0.1) { // Allow answers within 1 decimal place
                correctAnswers++;

                // Update correct answer counts
                String operationType = getOperationType(question);
                correctCounts.put(operationType, correctCounts.getOrDefault(operationType, 0) + 1);
            } else {
                wrongQuestions.add(question + " (Your Answer: " + userAnswer + ", Correct Answer: " + correctResult + ")");
            }

            // Update operation counts
            String operationType = getOperationType(question);
            operationCounts.put(operationType, operationCounts.getOrDefault(operationType, 0) + 1);

            // Display remaining time for every 10 questions
            if (i % 1 == 0) {
                System.out.println("Time remaining: " + remainingTime + " minutes");
            }
        }

        try {
            String filename = studentName.replaceAll("\\s+", "_") + "_results.txt";
            FileWriter fileWriter = new FileWriter(filename, true);
            fileWriter.write(studentName + ":\n");
            fileWriter.write("Correct Answers: " + correctAnswers + "\n");
            fileWriter.write("Incorrect Answers: " + (totalQuestions - correctAnswers) + "\n");
            fileWriter.write("\nOperation Counts:\n");
            for (Map.Entry<String, Integer> entry : operationCounts.entrySet()) {
                String operationType = entry.getKey();
                int count = entry.getValue();
                fileWriter.write(operationType + " Count: " + count + "\n");
                if (correctCounts.containsKey(operationType)) {
                    fileWriter.write(operationType + " Correct: " + correctCounts.get(operationType) + "\n");
                }
            }

            if (!wrongQuestions.isEmpty()) {
                fileWriter.write("\nWrong Questions:\n");
                for (String question : wrongQuestions) {
                    fileWriter.write(question + "\n");
                }
            }
            fileWriter.write("\n");
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Quiz completed!");
        System.out.println("Total correct answers: " + correctAnswers);

        int grade = calculateGrade(correctAnswers);
        System.out.println("Grade: " + getGradeText(grade));
    }

    private static int calculateGrade(int correctAnswers) {
        if (correctAnswers <= 4) {
            return 0;
        } else if (correctAnswers <= 10) {
            return 1;
        } else if (correctAnswers <= 16) {
            return 2;
        } else {
            return 3;
        }
    }

    private static String getGradeText(int grade) {
        switch (grade) {
            case 0:
                return "Fail";
            case 1:
                return "Pass";
            case 2:
                return "Merit";
            case 3:
                return "Distinction";
            default:
                return "Invalid Grade";
        }
    }

    private static String getOperationType(String question) {
        if (question.contains("+")) {
            return "Addition";
        } else if (question.contains("-")) {
            return "Subtraction";
        } else if (question.contains("*")) {
            return "Multiplication";
        } else if (question.contains("/")) {
            return "Division";
        } else {
            return "Unknown";
        }
    }
}
