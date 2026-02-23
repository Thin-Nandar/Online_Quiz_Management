package common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import common.User;
import common.Question;

public interface ExamInterface extends Remote {

    boolean addUser(String username, String password) throws RemoteException;
    boolean login(String username, String password) throws RemoteException;
    void editUser(String oldUsername, User newUser) throws RemoteException;
    List<User> searchUsers(String keyword) throws RemoteException;
    List<String[]> getUsers() throws RemoteException;

    // Question Management
    List<Question> getQuestions1() throws RemoteException;   // student exam page ကိုသုံး
    void editQuestion(int index, Question newQuestion) throws RemoteException;
    void deleteQuestion(int index) throws RemoteException;

    // Exam
    int submitAnswers(List<String> answers) throws RemoteException;
	void addQuestion(String[] questionData) throws RemoteException;
	/** Legacy: List<String[]> for old clients. Each item length=6: [Q, A, B, C, D, Correct] */
	void addQuestion(Question question) throws RemoteException;
	List<String[]> getQuestions() throws RemoteException;
    int getExamTime() throws RemoteException;
	void setExamTime(int seconds) throws RemoteException;

}
