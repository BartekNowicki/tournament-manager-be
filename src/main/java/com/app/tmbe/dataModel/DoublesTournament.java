package com.app.tmbe.dataModel;

import com.app.tmbe.enumConverter.TournamentType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, property = "@id")
@Entity
@Getter
@Setter
@NoArgsConstructor
// lombok does not deal well with inheritance, normal constructor implemented below
// @AllArgsConstructor
@Table(name = "doubles_tournaments")
public class DoublesTournament extends Tournament {

  // Bidirectional @ManyToMany, two parents, no children, one owner (Player)
  //@JsonBackReference
  @ManyToMany(mappedBy = "playedDoublesTournaments")
  private Set<Team> participatingTeams = new HashSet<>();

  // Bidirectional @OneToMany, two parents, no children, one owner (GroupInDoubles)
  @OneToMany(mappedBy = "partOfDoublesTournament")
  private Set<GroupInDoubles> groups;

  public DoublesTournament(
      long id,
      TournamentType type,
      Date startDate,
      Date endDate,
      int groupSize,
      String comment,
      Set<Team> participatingTeams) {
    super(id, type, startDate, endDate, groupSize, comment);
    this.participatingTeams = participatingTeams;
  }

  public void addTeam(Team team) {
    this.participatingTeams.add(team);
    team.getPlayedDoublesTournaments().add(this);
  }

  public void removeTeam(Team team) {
    this.participatingTeams.remove(team);
    team.getPlayedDoublesTournaments().remove(this);
  }

  @Override
  public String toString() {
    return "DoublesTournament{"
        + "participatingTeams="
        + participatingTeams.size()
        + ", groups="
        + groups.size()
        + "} "
        + super.toString();
  }
}
