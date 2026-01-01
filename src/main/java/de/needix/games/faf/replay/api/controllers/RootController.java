package de.needix.games.faf.replay.api.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

@CrossOrigin
@Controller
public class RootController {
    public static final Logger LOGGER = LoggerFactory.getLogger(RootController.class);

    @GetMapping("/")
    public String redirectToSwagger() {
        return "redirect:/index.html";
    }

    public static <Repo extends CrudRepository<E, Long>, E> void saveEntityInDatabase(Repo repo, E entity) {
        long startTime = System.currentTimeMillis();

        try {
            repo.save(entity);
        } catch (Exception e) {
            LOGGER.error("Failed to save entity to database: {}", e.getMessage(), e);
            throw e;
        }

        if (LOGGER.isInfoEnabled()) {
            long endTime = System.currentTimeMillis();
            LOGGER.info("Entity {} saved in {} ms", entity, endTime - startTime);
        }
    }
}