package app.models;

public class User {
    private String usuario;
    private String senha;

    public User() {}

    public User(String usuario, String senha){
        this.usuario = usuario;
        this.senha = senha;
    }
    
    public String getUsuario() {
        return usuario;
    }
    public String getSenha() {
        return senha;
    }
}
