package ua.nure.pihnastyi.practice8.db.entity;

public class UserGroup {

    private int idUser;

    private int idGroup;

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public int getIdGroup() {
        return idUser;
    }

    public void setIdGroup(int idGroup) {
        this.idUser = idGroup;
    }

    @Override
    public String toString() {
        return "User [id_user=" + idUser + ", id_group=" + idGroup + "]";
    }

}