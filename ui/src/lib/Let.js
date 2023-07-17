import React from 'react'

export default function Let({ children, ...p }) {
  return <>{children(p)}</>
}
