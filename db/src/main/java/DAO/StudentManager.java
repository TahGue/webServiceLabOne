package DAO;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import models.Student;

import java.util.List;

public class StudentManager {
    static EntityManagerFactory emf = Persistence.createEntityManagerFactory("default");

    public static Student createStudent(String name , String email , String tel , String image){

        Student student = new Student(name,email,tel,image);

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(student);
        em.getTransaction().commit();
        em.close();
        return student;
    }

    public static void updateStudent(int id,String name , String email , String tel , String image){
        EntityManager em = emf.createEntityManager();
        Student student = em.find(Student.class,id);
        em.getTransaction().begin();
        student.setName(name);
        student.setEmail(email);
        student.setTel(tel);
        student.setImage(image);
        em.getTransaction().commit();
        em.close();
    }

    public  static List<Student> fetchAll(){
        System.out.println("fetch all from manager");
        EntityManager em = emf.createEntityManager();
        System.out.println(em);
        List<Student> students = em.createQuery("SELECT c FROM Student c",Student.class).getResultList();
        em.close();
        return students;
    };
}
