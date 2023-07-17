import React from 'react'
import PropTypes from 'prop-types'
import cx from 'classnames'
import Spinner from './Spinner'

export default function ProfileMenuButton({ profile, isSelected, isLoading, onClick }) {
  return (
    <button
      className={cx(
        'btn btn-secondary rounded-pill',
        isSelected ? 'ui-menu-item-selected leihs-menu-item--selected' : ''
      )}
      onClick={onClick}
    >
      <div className="d-inline-block position-relative">
        {profile.shortName}
        {isLoading && (
          <Spinner
            className="position-absolute text-muted"
            style={{ left: '-0.2rem', top: '-0.1rem', opacity: '1', width: '1.6rem', height: '1.6rem' }}
          />
        )}
      </div>{' '}
      • {profile.profileName}
    </button>
  )
}

const profileShape = {
  id: PropTypes.string.isRequired,
  profileName: PropTypes.string.isRequired,
  shortName: PropTypes.string
}

ProfileMenuButton.propTypes = {
  profile: PropTypes.shape(profileShape).isRequired,
  isSelected: PropTypes.bool,
  isLoading: PropTypes.bool,
  onClick: PropTypes.func
}
