package com.nekomart.services;
import com.nekomart.dao.UsuarioDAO;
import com.nekomart.models.Usuario;
import java.util.List;

public class UsuarioService {
    private UsuarioDAO dao = new UsuarioDAO();

    public List<Usuario> obtenerTodos() { return dao.listarTodos(); }
    
    public boolean crearUsuario(Usuario u, String pass) {
        if (pass.length() < 4) return false;
        return dao.crear(u, pass);
    }

    public boolean eliminarUsuario(int id) { return dao.eliminar(id); }
}