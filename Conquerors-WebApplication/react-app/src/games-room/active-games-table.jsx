import React, { Component } from 'react';

class ActiveGamesTable extends Component {

  constructor(){
    super();
  }

  // Populate the table with data based on the received data from the parent component
  createTable() {
    let table = []
    let gamesData = this.props.gamesData;
    
    for (let i = 0; i < gamesData.length; i++) {
        let children = []
  
        children.push(<td key={gamesData[i].title+"-"+"title"}>{gamesData[i].title}</td>)
        children.push(<td key={gamesData[i].title+"-"+"uploadedBy"}>{gamesData[i].uploadedBy}</td>)
        children.push(<td key={gamesData[i].title+"-"+"status"}>{gamesData[i].status}</td>)
        children.push(<td key={gamesData[i].title+"-"+"boardSize"}>{gamesData[i].boardSize}</td>)
        children.push(<td key={gamesData[i].title+"-"+"playersInGame"}>{gamesData[i].playersInGame}</td>)
        children.push(<td key={gamesData[i].title+"-"+"viewBoardButton"}>
          <button className="my-button" onClick={() => this.props.openViewGameUnitsDialog(gamesData[i].units)}>View Units</button></td>)
        children.push(<td key={gamesData[i].title+"-"+"viewUnitsButton"}>
          <button className="my-button" onClick={() => this.props.openViewGameBoardDialog(gamesData[i].board)}>View Board</button></td>)
        children.push(<td key={gamesData[i].title+"-"+"joinGameButton"}>
          <button className="my-button" onClick={() => this.props.handleJoinGame(gamesData[i].title)} disabled={gamesData[i].status != "Pending"}>Join Game</button></td>)

        table.push(<tr key={"game-row-"+gamesData[i].title} >{children}</tr>)
    }
    return table;
  }

  render() {
    return (
      <div className={"table-container" + (this.props.hidden ? " hidden" : "")}>
          {
            this.props.gamesData.length != 0 ? 
            <div>
              <table className="my-table">
                <thead>
                  <tr>
                    <th>Title</th>
                    <th>Uploaded By</th>
                    <th>Status</th>
                    <th>Board Size</th>
                    <th>Players In-Game</th>
                    <th></th>
                    <th></th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  {this.createTable()}
                </tbody>
              </table>
            </div> : 
            <div style={{margin: "0px 20px"}}>There are currently no active games available.</div>
          }
      </div>
    );
  }
}

export default ActiveGamesTable; 
