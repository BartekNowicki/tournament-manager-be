package com.app.tmbe.service;

import com.app.tmbe.dataModel.GroupInDoubles;
import com.app.tmbe.dataModel.GroupInSingles;
import com.app.tmbe.dataModel.Player;
import com.app.tmbe.dataModel.SinglesTournament;
import com.app.tmbe.dataModel.Team;
import com.app.tmbe.dataModel.DoublesTournament;
import com.app.tmbe.exception.NoEntityFoundCustomException;
import com.app.tmbe.repository.DoublesTournamentRepository;
import com.app.tmbe.repository.GroupInDoublesRepository;
import com.app.tmbe.repository.TeamRepository;
import com.app.tmbe.utils.GrouperInterface;
import com.app.tmbe.utils.TeamGrouper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TeamService {
  @Autowired TeamRepository teamRepository;
  @Autowired DoublesTournamentRepository doublesTournamentRepository;
  @Autowired GroupInDoublesRepository groupInDoublesRepository;

  public List<Team> getAllTeams() {
    return teamRepository.findAll();
  }

  public List<Team> getAllTeamsOrderByIdAsc() {
    return teamRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
  }

  public Optional<Team> getTeamById(long id) {
    return teamRepository.findById(id);
  }

  public Team deleteTeamById(long id) throws NoEntityFoundCustomException {
    Optional<Team> teamToDelete = teamRepository.findById(id);
    if (teamToDelete.isEmpty()) {
      throw new NoEntityFoundCustomException("No team with that id exists: " + id);
    }
    Team teamToBeDeleted = teamToDelete.get();
    // need a new hashset to avoid the concurrent modification exception
    for (DoublesTournament doublesTournament :
        new HashSet<>(teamToBeDeleted.getPlayedDoublesTournaments())) {
      teamToBeDeleted.removeDoublesTournament(doublesTournament);
    }
    teamRepository.delete(teamToBeDeleted);
    return teamToBeDeleted;
  }

  public Team saveOrUpdateTeam(Team team) {
    Optional<Team> teamToUpdate = teamRepository.findById(team.getId());
    if (teamToUpdate.isEmpty()) {
      for (DoublesTournament doublesTournament : team.getPlayedDoublesTournaments()) {
        doublesTournament.addTeam(team);
      }
      return teamRepository.save(team);
    } else {
      Team t = teamToUpdate.get();
      Set<DoublesTournament> totalTournamentsPlayed =
          new HashSet<>(team.getPlayedDoublesTournaments());
      totalTournamentsPlayed.addAll(t.getPlayedDoublesTournaments());
      t.setPlayerOneId(team.getPlayerOneId());
      t.setPlayerTwoId(team.getPlayerTwoId());
      t.setComment(team.getComment());
      t.setIsChecked(team.getIsChecked());
      t.setStrength(team.getStrength());
      t.setPlayedDoublesTournaments(totalTournamentsPlayed);
      return teamRepository.save(t);
    }
  }

  public Set<Team> findAllByIsChecked(boolean b) {
    return teamRepository.findByIsChecked(true);
  }

  public Map<Integer, Set<Team>> groupTeams(long doublesTournamentId) throws Exception {
    // using the repo instead of the service to avoid the prohibited circular dependency
    // playerService <-> tournamentService

    DoublesTournament tournament =
        doublesTournamentRepository
            .findById(doublesTournamentId)
            .orElseThrow(() -> new IllegalArgumentException("Invalid doubles tournamentId"));

    if (tournament.getGroups().size() > 0) {
      throw new Exception("This tournament already has groups assigned to it!");
    }

    int groupSize = tournament.getGroupSize();
    Set<Team> teams =
        tournament.getParticipatingTeams().stream()
            .filter(t -> t.getId() != -1)
            .collect(Collectors.toSet());
    GrouperInterface teamGrouper = new TeamGrouper(teams, groupSize);
    Map<Integer, Set<Team>> groups = teamGrouper.groupTeams();

    groups
        .entrySet()
        .forEach(
            entry -> {
              // this is a dummy in the sense that entry.getKey() will get reassigned by the db as
              // autoincremented id
              GroupInDoubles dummy =
                  new GroupInDoubles(entry.getKey(), entry.getValue(), tournament);
              GroupInDoubles newGroup = groupInDoublesRepository.save(dummy);
              for (Team t : new HashSet<>(newGroup.getMembers())) {
                t.joinGroup(newGroup);
                saveOrUpdateTeam(t);
              }
              tournament.addGroup(newGroup);
            });
    doublesTournamentRepository.save(tournament);
    return groups;
  }

  public List<Team> unGroupTeams(long doublesTournamentId) throws Exception {
    // using the repo instead of the service to avoid the prohibited circular dependency
    // teamService <-> tournamentService

    DoublesTournament tournament =
        doublesTournamentRepository
            .findById(doublesTournamentId)
            .orElseThrow(() -> new IllegalArgumentException("Invalid doubles tournamentId"));

    if (tournament.getGroups().size() == 0) {
      throw new Exception("This tournament has no groups assigned to it!");
    }

    Set<GroupInDoubles> groups = new HashSet<>(tournament.getGroups());

    groups.forEach(
        g -> {
          for (Team t : new HashSet<>(g.getMembers())) {
            t.leaveGroup(g);
            saveOrUpdateTeam(t);
          }
          tournament.removeGroup(g);
          groupInDoublesRepository.delete(g);
        });

    doublesTournamentRepository.save(tournament);

    return teamRepository.findAll();
  }

  public Map<Long, Boolean> checkTeams(Map<Long, Boolean> idToCheckStatusMapping) throws Exception {

    Map<Long, Boolean> outcome = new HashMap<>();

    for (Long id : idToCheckStatusMapping.keySet()) {
      try {
        Team team =
            teamRepository
                .findById(id)
                .orElseThrow(
                    () -> new Exception("Batch check failed, team not found by id: " + id));
        team.setIsChecked(idToCheckStatusMapping.get(id));
        Team updatedTeam = teamRepository.save(team);
        outcome.put(updatedTeam.getId(), updatedTeam.getIsChecked());

      } catch (Exception e) {
        e.printStackTrace();
        throw new Exception("Rethrowing: Batch check failed, team not found by id: " + id);
      }
    }
    return outcome;
  }
}
