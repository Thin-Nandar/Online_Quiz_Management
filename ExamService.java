package server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import common.Activity;
import common.Question;
import common.User;

public interface ExamService extends Remote {
 
	boolean login(String username, String password) throws RemoteException;
    List<Question> getQuestions() throws RemoteException;
    int submitAnswers(List<String> answers) throws RemoteException;
	User[] getAllUsers();
	boolean addUser(String trim, User u);
	void logActivity(Activity activity);



}


