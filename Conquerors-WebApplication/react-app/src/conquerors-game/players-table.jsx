import React, { Component } from 'react';

class PlayersTable extends Component {

  constructor(props){
    super(props);
  }

  colorTableRow(playerRowData){
    let currentPlayer = this.props.currentPlayer;
    let rowColor;
    if(playerRowData.surrendered)
      rowColor = "black";
    else if(currentPlayer != null && currentPlayer.id == playerRowData.id)
      rowColor = playerRowData.color;
    else
      rowColor = null;

    return {background: rowColor};
  }

  // Populate the table with data based on the received data from the parent component
  createTable() {
    let table = [];
    let playersData = this.props.playersData;

    for (let i = 0; i < playersData.length && playersData[i] != null; i++) {
        let children = [];
  
        children.push(<td key={playersData[i].id+"-"+i+"name"}>{playersData[i].name}</td>)
        children.push(<td key={playersData[i].id+"-"+i+"id"}>{playersData[i].id}</td>)
        children.push(<td key={playersData[i].id+"-"+i+"color"} style={{background: playersData[i].surrendered ? null : playersData[i].color}}>{playersData[i].colorName}</td>)
        children.push(<td key={playersData[i].id+"-"+i+"money"}>{playersData[i].money}</td>)

        table.push(
          <tr 
            key={playersData[i].id} 
            data-id={playersData[i].id} 
            style={this.colorTableRow(playersData[i])}>
            {children}
          </tr>
        )
    }
    return table;
  }

  render() {
    return (
      <div className={"table-container" + (this.props.hidden ? " hidden" : "")}>
        <table className="my-table">
          <thead>
            <tr>
              <th>Name</th>
              <th>ID</th>
              <th>Color</th>
              <th>Money</th>
            </tr>
          </thead>
          <tbody>
            {this.createTable()}
          </tbody>
        </table>
      </div>
    );
  }
}

export default PlayersTable; 
