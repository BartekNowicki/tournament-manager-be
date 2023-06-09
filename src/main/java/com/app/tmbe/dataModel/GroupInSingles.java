package com.app.tmbe.dataModel;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "groups_in_singles")
public class GroupInSingles {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id")
  private long id;

  // Bidirectional @ManyToMany, two parents, no children, one owner (Player)
  @JsonBackReference(value = "groupsinsingles-members")
  @ManyToMany(mappedBy = "belongsToSinglesGroups")
  private Set<Player> members = new HashSet<>();

  // Bidirectional @OneToMany, two parents, no children, one owner (GroupInSingles)
  @JsonBackReference(value = "groupinsingles-partofsinglestournament") // new
  @ManyToOne
  @JoinColumn(name = "singles_tournament_id")
  private SinglesTournament partOfSinglesTournament;

  @Override
  public String toString() {
    return "GroupInSingles{"
        + "id="
        + id
        + ", members="
        + members
        + ", partOfSinglesTournament="
        + partOfSinglesTournament.getId()
        + '}';
  }
}
