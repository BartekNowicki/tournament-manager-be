package com.app.tmbe.dataModel;

import com.app.tmbe.enumConverter.TournamentType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
// lombok does not deal well with inheritance, normal constructor implemented below
// @AllArgsConstructor
@Table(name = "singles_tournaments")
public class SinglesTournament extends Tournament {

  // Bidirectional @ManyToMany, two parents, no children, one owner (Player)
  @JsonBackReference
  @ManyToMany(mappedBy = "playedSinglesTournaments")
  private Set<Player> participatingPlayers = new HashSet<>();

  public SinglesTournament(
      long id,
      TournamentType type,
      Date startDate,
      Date endDate,
      int groupSize,
      String comment,
      Set<Player> participatingPlayers) {
    super(id, type, startDate, endDate, groupSize, comment);
    this.participatingPlayers = participatingPlayers;
  }

//  public SinglesTournament(Set<Player> participatingPlayers) {
//    this.participatingPlayers = participatingPlayers;
//  }

  public void addPlayer(Player player) {
    this.participatingPlayers.add(player);
    player.getPlayedSinglesTournaments().add(this);
  }

  public void removePlayer(Player player) {
    this.participatingPlayers.remove(player);
    player.getPlayedSinglesTournaments().remove(this);
  }

  @Override
  public String toString() {
    return "SinglesTournament{"
        + "id="
        + super.getId()
        + ", type='"
        + super.getType()
        + '\''
        + ", startDate="
        + super.getStartDate()
        + ", endDate="
        + super.getEndDate()
        + ", groupSize="
        + super.getGroupSize()
        + ", comment='"
        + super.getComment()
        + '\''
        + ", players="
        + participatingPlayers.size()
        + '}';
  }
}
