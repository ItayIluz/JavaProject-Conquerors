import React, { Component } from 'react';

class TerritoryArmiesErrorDialog extends Component {

  constructor(props){
    super(props);
  }

  render() {
    return (
      <div className="container-header dialog-header">
        <div className="container-header-title">Error!</div>
        <div className="container dialog-container">
          <div style={{margin: "0px 20px"}}>
            <u><b>{this.props.errorMessage}</b></u>
          </div>
          <div className="dialog-button-panel">
            <button className="my-button dialog-button" onClick={this.props.closeFunction}>Close</button>
          </div>
        </div>
      </div>
    );
  }
}

export default TerritoryArmiesErrorDialog; 