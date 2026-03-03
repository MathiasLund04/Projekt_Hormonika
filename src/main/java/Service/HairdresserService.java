package Service;

import DAL.DBConfig;
import Exceptions.DataAccessException;
import Model.Hairdresser;
import Repository.Hairdresser.HairdresserRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class HairdresserService {

    private final DBConfig db;
    private List<Hairdresser> hairdressers;
    public HairdresserRepository hRepo;

    public HairdresserService(HairdresserRepository hRepo, DBConfig db) {
        this.db = db;
        this.hRepo = hRepo;

        try {
             hairdressers = new ArrayList<>(hRepo.getHairdressers());

        } catch (SQLException e){
            throw new DataAccessException("Kunne ikke indlæse frisører");
        }
    }
    public List<Hairdresser> getHairdressers() {
        try {
            hRepo.getHairdressers();
        } catch (SQLException e){
            throw new DataAccessException("Kunne ikke indlæse frisører fra DB");
        }

        return new ArrayList<>(hairdressers);
    }
    public void addHairdresser(Hairdresser hairdresser) {
        hairdressers.add(hairdresser);
        //WIP
    }
    public Optional<Hairdresser> getHairdresserById(int id) {
        for (Hairdresser hd : hairdressers) {
            if (hd.getId() == id) {
                return Optional.of(hd);
            }
        }
        return Optional.empty();
    }

    // Login Metode
    public Optional<Hairdresser> login(String username, String password) throws DataAccessException {
        try {
            List<Hairdresser> existing = hRepo.getHairdressers();

            for (Hairdresser hd : existing) {
                if (hd.getUsername().equals(username) && hd.getPassword().equals(password)) {
                    return Optional.of(hd);
                } else {
                    return Optional.empty();
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Fejl ved login", e);
        }
        return Optional.empty();
    }

}
