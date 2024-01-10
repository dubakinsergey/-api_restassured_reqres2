package api.reqres.registration;

public class SuccesReg {
    private Integer id;
    private String token;

    public SuccesReg(Integer id, String token) {
        this.id = id;
        this.token = token;
    }

    public Integer getId() {
        return id;
    }

    public String getToken() {
        return token;
    }
}
