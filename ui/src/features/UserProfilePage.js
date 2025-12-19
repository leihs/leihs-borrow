import React from 'react'
import PropTypes from 'prop-types'
import cx from 'classnames'
import PageLayout from '../components/PageLayout'
import Section from '../components/Section'
import PropertyTable from '../components/PropertyTable'
import ListCard from '../components/ListCard'
import DownloadLink from '../components/DownloadLink'
import ActionButtonGroup from '../components/ActionButtonGroup'

const BASE_CLASS = 'ui-user-profile'

function UserProfilePage({ txt, user, delegations, contracts, onLogoutClick, ...restProps }) {
  const {
    pageTitle,
    sectionUserData,
    sectionContracts,
    sectionDelegations,
    logout,
    noContracts,
    emailField,
    secondaryEmailField,
    phoneField,
    orgField,
    orgIdField,
    badgeIdField
  } = txt

  const isLocalUser = user.organization === 'local'

  const userDataTable = [
    [emailField, user.email],
    user.secondaryEmail && [secondaryEmailField, user.secondaryEmail],
    [phoneField, user.phone],

    ...(isLocalUser
      ? []
      : [
          [orgField, user.organization],
          [orgIdField, user.orgId]
        ]),

    user.badgeId && [badgeIdField, user.badgeId]
  ]
    .filter(Boolean)
    .map(([key, value]) => ({ key, value }))

  return (
    <div {...restProps} className={cx(restProps.className, BASE_CLASS)}>
      <PageLayout.Header title={pageTitle} subTitle={user.name}>
        {!!onLogoutClick && (
          <ActionButtonGroup>
            <button type="button" className="btn btn-secondary" onClick={onLogoutClick}>
              {logout}
            </button>
          </ActionButtonGroup>
        )}
      </PageLayout.Header>
      <div className="d-grid gap-5">
        <Section title={sectionUserData} collapsible>
          <PropertyTable properties={userDataTable} />
        </Section>
        {!!delegations.length && (
          <Section title={sectionDelegations} collapsible>
            <ListCard.Stack>
              {delegations.map(({ id, name, responsibleName, responsibleEmail, href }) => (
                <ListCard key={id} href={href}>
                  <ListCard.Title>
                    {href ? (
                      <a href={href} className="stretched-link">
                        {name}
                      </a>
                    ) : (
                      name
                    )}
                  </ListCard.Title>
                  <ListCard.Body>
                    <div>{responsibleName}</div>
                    {responsibleEmail && (
                      <div className="pt-1">
                        <a href={'mailto:' + responsibleEmail}>{responsibleEmail}</a>
                      </div>
                    )}
                  </ListCard.Body>
                </ListCard>
              ))}
            </ListCard.Stack>
          </Section>
        )}
        <Section title={sectionContracts} collapsible>
          {!contracts.length ? (
            <p>{noContracts}</p>
          ) : (
            <div className="d-grid gap-3">
              {contracts.map(({ id, downloadUrl, displayName }) => {
                return (
                  <DownloadLink key={id} href={downloadUrl} target="_blank">
                    {displayName}
                  </DownloadLink>
                )
              })}
            </div>
          )}
        </Section>
      </div>
    </div>
  )
}

UserProfilePage.propTypes = {
  user: PropTypes.object.isRequired,
  delegations: PropTypes.array.isRequired,
  contracts: PropTypes.array.isRequired,
  onLogoutClick: PropTypes.func,
  txt: PropTypes.any
}

export default UserProfilePage
