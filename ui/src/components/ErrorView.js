import React from 'react'
import ActionButtonGroup from './ActionButtonGroup'

export default function ErrorView({ title = 'Error', message, details, actions = [] }) {
  return (
    <div className="alert alert-danger">
      <h2>{title}</h2>
      {message && <div className="mb-3">{message}</div>}
      {details && (
        <details className="mb-3">
          <div className="preserve-linebreaks small">{details}</div>
        </details>
      )}
      {actions && actions.length > 0 && (
        <ActionButtonGroup>
          {actions.map((a, i) => {
            const { onClick, href, title, variant } = a
            const btnClass = variant === 'link-button' ? 'btn btn-link' : 'btn btn-dark'
            return href ? (
              <a key={i} onClick={onClick} href={href} className={btnClass}>
                {title}
              </a>
            ) : (
              <button key={i} onClick={onClick} className={btnClass}>
                {title}
              </button>
            )
          })}
        </ActionButtonGroup>
      )}
    </div>
  )
}
