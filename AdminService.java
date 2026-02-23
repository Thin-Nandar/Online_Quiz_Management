package server;

import common.User;
import common.Question;
import common.Activity;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface AdminService extends java.rmi.Remote {
	void setExamTime(int seconds) throws RemoteException;
    // User Management
    List<User> getAllUsers() throws RemoteException;
    boolean addUser(String username, String password) throws RemoteException;
    void editUser(String oldUsername, User newUser) throws RemoteException;
    void deleteUser(String username) throws RemoteException;
    void resetPassword(String username, String newPassword) throws RemoteException;


    // Question Management
    void addQuestion(Question q) throws RemoteException;
    void editQuestion(int index, Question newQ) throws RemoteException;
    void deleteQuestion(int index) throws RemoteException;

    // Activity Logging
    void logActivity(Activity a) throws RemoteException;
    List<Activity> getAllActivities() throws RemoteException;
	List<Question> getAllQuestions() throws RemoteException;
	List<User> searchUsers(String keyword) throws RemoteException;
	boolean login(String username, String password) throws RemoteException;
   int getExamTime() throws RemoteException;   // ✅ must throw RemoteException

}