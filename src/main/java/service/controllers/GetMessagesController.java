package service.controllers;


import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.entity.Message;
import service.entity.Token;
import service.entity.User;
import service.objects.ErrorResponseObject;
import service.objects.MessageInResponse;
import service.objects.MessageResponse;
import service.repository.MessageRepository;
import service.repository.TokenRepository;
import service.repository.UserRepository;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static service.controllers.SendMessageController.checkToken;
import static service.controllers.SendMessageController.checkUser;


@RestController
@RequestMapping("/{auth_token}/messages/{dialog_id}")
public class GetMessagesController {
    private static final Logger log = Logger.getLogger(GetMessagesController.class.getName());

    @RequestMapping(value = "/after/{timestamp}")
    ResponseEntity<?> getCustomMessageAfterSomeTime
            (
                    @PathVariable String auth_token,
                    @PathVariable String dialog_id,
                    @PathVariable long timestamp
            ) {


        return getMessages(auth_token, dialog_id, DEFAULT_COUNT_OF_MESSAGES, 0, timestamp);
    }

    @RequestMapping(value = "/after/{timestamp}/limit/{limit}")
    ResponseEntity<?> getCustomMessageAfterSomeTimeWithLimit
            (
                    @PathVariable String auth_token,
                    @PathVariable String dialog_id,
                    @PathVariable int limit,
                    @PathVariable long timestamp
            ) {
        return getMessages(auth_token, dialog_id, limit, 0, 0);
    }

    @RequestMapping
    ResponseEntity<?> getDefaultMessageCount
            (
                    @PathVariable String auth_token,
                    @PathVariable String dialog_id
            ) {
        return getMessages(auth_token, dialog_id, DEFAULT_COUNT_OF_MESSAGES, 0, 0);
    }

    @RequestMapping(value = "/limit/{limit}")
    ResponseEntity<?> getCustomMessageCountWithoutOffset
            (
                    @PathVariable String auth_token,
                    @PathVariable String dialog_id,
                    @PathVariable int limit
            ) {
        return getMessages(auth_token, dialog_id, limit, 0, 0);
    }

    @RequestMapping(value = "/limit/{limit}/skip/{offset}")
    ResponseEntity<?> getCustomMessageCountWithCustomOffset
            (
                    @PathVariable String auth_token,
                    @PathVariable String dialog_id,
                    @PathVariable int limit,
                    @PathVariable int offset
            ) {


        return getMessages(auth_token, dialog_id, limit, offset, 0);
    }

    /**
     * template request
     * @param auth_token secret token
     * @param dialog_id id of dialog
     * @param limit limit of messages
     * @param skip how much messages should be skipped
     * @param timestamp UNIX timestapm
     * @return suitable response <Error suitable too>
     */
    private ResponseEntity<?> getMessages(
            String auth_token,
            String dialog_id,
            int limit,
            int skip,
            long timestamp
    ) {
        Token token = tokenRepository.findByToken(auth_token);
        if (checkToken(auth_token, token)) {
            log.info("invalid token");
            return new ResponseEntity<>(new ErrorResponseObject("invalid token"), HttpStatus.FORBIDDEN);
        }

        User user = userRepository.findByUsername(token.getUsername());
        if (checkUser(tokenRepository, auth_token, token, user)) {
            log.info("token without username");
            return new ResponseEntity<>(new ErrorResponseObject("invalid token"), HttpStatus.FORBIDDEN);
        }

        MessageResponse response = getMessageResponse(dialog_id, limit, skip, timestamp, user);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * create response for message dialog request
     * @param dialog_id id of dialog
     * @param limit limit of messages
     * @param skip how much messages should be skipped
     * @param timestamp UNIX timestapm
     * @param toUser ref to User from which messages
     * @return suitable response
     */
    private MessageResponse getMessageResponse(String dialog_id, int limit, int skip, long timestamp, User toUser) {
        MessageResponse response = new MessageResponse();


        List<MessageInResponse> messageList = response.getMessages();

        //get all messages
        List<Message> toFrom = messageRepository.findByToUsernameAndFromUsername(toUser.getUsername(),dialog_id);
        List<Message> FromTo = messageRepository.findByToUsernameAndFromUsername(dialog_id,toUser.getUsername());

        toFrom.addAll(FromTo);


        //filter messages
        List<Message> dialog =  toFrom.stream().filter(
                l ->
                        ( l.getFromUsername().equals(dialog_id)||l.getToUsername().equals(dialog_id))
                                && l.getTimestamp() > timestamp
        ).collect(Collectors.toList());

        //fill response object
        for (int i = skip; i < dialog.size() && i - skip <= limit; i++) {
            messageList.add(new MessageInResponse(toFrom.get(i).getFromUsername(),toFrom.get(i)));
        }
        return response;
    }

    private static final int DEFAULT_COUNT_OF_MESSAGES = 25;


    private UserRepository userRepository;
    private TokenRepository tokenRepository;
    private MessageRepository messageRepository;

    @Autowired
    public GetMessagesController(UserRepository userRepository, TokenRepository tokenRepository, MessageRepository messageRepository) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.messageRepository = messageRepository;
    }


}