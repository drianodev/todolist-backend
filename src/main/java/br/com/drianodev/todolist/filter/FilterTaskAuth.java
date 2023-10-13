package br.com.drianodev.todolist.filter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.drianodev.todolist.user.IUserRepository;
import br.com.drianodev.todolist.user.UserModel;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

    @Autowired
    private IUserRepository userRepository;

    private UserModel userModel;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        var servletPath = request.getServletPath();

        if (servletPath.startsWith("/tasks/")) {

            var authorization = request.getHeader("Authorization");

            if (authorization == null) {
                response.sendError(401);
                return;
            }


            var authEncoded = authorization.substring("Basic".length()).trim();

            byte[] authDecode = Base64.getDecoder().decode(authEncoded);

            var authString = new String(authDecode);

            String[] credentials = authString.split(":");
            String username = credentials[0];
            String password = credentials[1];

            var user = this.userRepository.findByUsername(username);

            if (!user.isPresent()) {
                response.sendError(401);
                return;
            }

            var passwordVerify = BCrypt.verifyer().verify(password.toCharArray(), user.get().getPassword());

            if (passwordVerify.verified) {
                request.setAttribute("idUser", user.get().getId());
                chain.doFilter(request, response);
            } else {
                response.sendError(401);
            }
        } else {
            chain.doFilter(request, response);
        }
    }
}
