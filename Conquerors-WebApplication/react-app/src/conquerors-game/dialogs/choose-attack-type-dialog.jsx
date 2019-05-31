import React, { Component } from 'react';

class ChooseAttackTypeDialog extends Component {

  constructor(props){
    super(props);
  }

  render() {
    return (
      <div className="container-header dialog-header">
        <div className="container-header-title">Choose Attack Type</div>
        <div className="container dialog-container">
          <div className="dialog-button-panel">
            <button className="my-button dialog-button" onClick={() => this.props.closeFunction("I'm Feeling Lucky")}>I'm Feeling Lucky</button>
            <button className="my-button dialog-button" onClick={() => this.props.closeFunction("Deterministic")}>Deterministic</button>
            <button className="my-button dialog-button" onClick={() => this.props.closeFunction("Cancel")}>Cancel</button>
          </div>
        </div>
      </div>
    );
  }
}

export default ChooseAttackTypeDialog; 