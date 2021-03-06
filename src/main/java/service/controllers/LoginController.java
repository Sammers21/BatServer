package service.controllers;


import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import service.entity.User;
import service.objects.RegisterUserObject;
import service.entity.Token;
import service.objects.ErrorResponseObject;
import service.repository.DialogRepository;
import service.repository.MessageRepository;
import service.repository.TokenRepository;
import service.repository.UserRepository;

@RestController
@RequestMapping("/login")
public class LoginController extends BaseController{
    private static final Logger log = Logger.getLogger(LoginController.class.getName());

    /**
     * method who responsible for login
     * @param input params to login
     * @return status
     */
    @RequestMapping(method = RequestMethod.POST)
    ResponseEntity<?> loginUser(
            @RequestBody RegisterUserObject input
    ) {
        log.info(String.format("someone call login with json : %s", input.toString()));

        User byUsername = userRepository.findByUsername(input.getUsername());

        if (byUsername == null) {
            log.error(" login fail with " + input.getUsername());
            return new ResponseEntity<>(new ErrorResponseObject("Invalid username or password"), HttpStatus.FORBIDDEN);
        } else if (byUsername.getPassword().equals(input.getPassword())) {
            Token token = Token.generate(tokenRepository, byUsername.getUsername());
            log.info("succesfull login with " + input.getUsername());
            log.info(input.getUsername() + " get token " + token.getToken());
            return new ResponseEntity<>(token.getJSONObject(), HttpStatus.OK);
        } else {
            log.error(" login fail with " + input.getUsername() + " and invalid password");
            return new ResponseEntity<>(new ErrorResponseObject("Invalid username or password"), HttpStatus.FORBIDDEN);
        }

    }
    @Autowired
    public LoginController(UserRepository userRepository, DialogRepository dialogRepository, TokenRepository tokenRepository, MessageRepository messageRepository) {
        super(userRepository, dialogRepository, tokenRepository, messageRepository);
    }
}
